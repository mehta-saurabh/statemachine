package org.rebel.lab.statemachine;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rebel.lab.statemachine.enums.Events;
import org.rebel.lab.statemachine.enums.JiraStates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest
class StatemachineApplicationTests {

    private StateMachine<JiraStates, Events> stateMachine;
    @Autowired
    private StateMachineFactory<JiraStates, Events> stateMachineFactory;

    @BeforeEach
    public void setup(){
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    public void initTest() {
        Assertions.assertThat(stateMachine.getState().getId())
                .isEqualTo(JiraStates.BACKLOG);

        Assertions.assertThat(stateMachine).isNotNull();
    }

    @Test
    public void testGreenFlow() {
        stateMachine.sendEvent(Events.START_FEATURE);
        stateMachine.sendEvent(Events.FINISH_FEATURE);
        stateMachine.sendEvent(Events.QA_TEAM_APPROVE);
        // Asserts
        Assertions.assertThat(stateMachine.getState().getId())
                .isEqualTo(JiraStates.DONE);
    }



    @Test
    public void testWrongWay() {
        // Arrange
        // Act
        stateMachine.sendEvent(Events.START_FEATURE);
        stateMachine.sendEvent(Events.QA_TEAM_APPROVE);
        // Asserts
        Assertions.assertThat(stateMachine.getState().getId())
                .isEqualTo(JiraStates.IN_PROGRESS);
    }



}
