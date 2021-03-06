package org.rebel.lab.statemachine.config;

import org.rebel.lab.statemachine.enums.Events;
import org.rebel.lab.statemachine.enums.JiraStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;

import java.util.Optional;

@Configuration
//@EnableStateMachine
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<JiraStates, Events> {
    private static Logger log = LoggerFactory.getLogger(StateMachineConfig.class);

    @Override
    public void configure(
            StateMachineConfigurationConfigurer
                    <JiraStates, Events> config) throws Exception {
        config.withConfiguration().listener(listener()).autoStartup(true);
    }

    @Override
    public void configure(
            StateMachineStateConfigurer<JiraStates, Events> states)
            throws Exception {
        states.withStates().initial(JiraStates.BACKLOG)
                .state(JiraStates.IN_PROGRESS)
                .state(JiraStates.TESTING)
                .end(JiraStates.DONE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<JiraStates, Events> transitions)
            throws Exception {
        transitions
                .withExternal().source(JiraStates.BACKLOG).target(JiraStates.IN_PROGRESS).event(Events.START_FEATURE)

                .and()
                .withExternal()
                .source(JiraStates.IN_PROGRESS)
                .target(JiraStates.TESTING)
                .event(Events.FINISH_FEATURE)
                .guard(checkDeployGuard())
                .action(deployAction())//moved action

                .and()
                .withExternal()
                .source(JiraStates.TESTING)
                .target(JiraStates.IN_PROGRESS)
                .event(Events.QA_TEAM_REJECT)

                .and()
                .withExternal()
                .source(JiraStates.TESTING)
                .target(JiraStates.DONE)
                .event(Events.QA_TEAM_APPROVE)

                .and()
                .withExternal()
                .source(JiraStates.BACKLOG)
                .target(JiraStates.TESTING)
                .guard(checkDeployGuard())
                .event(Events.DEV_TEST)

                .and()
                .withInternal()
                .source(JiraStates.IN_PROGRESS)
                .event(Events.DEPLOY)
                .action(deployAction());
        ;
    }

    private StateMachineListener<JiraStates, Events> listener() {
        return new StateMachineListenerAdapter<JiraStates, Events>(){
            @Override
            public void transition(Transition<JiraStates, Events> transition) {
                log.warn("move from:{} to:{}",transition.getSource(),transition.getTarget());
            }

            @Override
            public void eventNotAccepted(Message<Events> event) {
                log.error("event not accepted: {}", event);
            }

        };
    }

    private Action<JiraStates, Events> deployAction() {
        return context -> {
            log.warn("DEPLOYING: {}",context.getEvent());
            context.getExtendedState()
                    .getVariables()
                    .put("deployed", true);
        };
    }

    private Guard<JiraStates, Events> checkDeployGuard() {
        return context -> {
            Boolean flag = (Boolean) context.getExtendedState()
                    .getVariables()
                    .get("deployed");
            return flag == null ? false : flag;
        };
    }


}
