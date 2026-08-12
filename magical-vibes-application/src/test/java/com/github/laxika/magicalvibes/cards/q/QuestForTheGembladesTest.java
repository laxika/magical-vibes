package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quest for the Gemblades")
class QuestForTheGembladesTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature offers a quest counter")
    void combatDamageToCreatureOffersQuestCounter() {
        Permanent quest = addQuest();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the quest")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent quest = addQuest();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing a quest counter and sacrificing puts four counters on target creature")
    void sacrificePutsFourCountersOnTargetCreature() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(quest);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability requires a quest counter and a creature target")
    void abilityRequiresCounterAndCreatureTarget() {
        Permanent quest = addQuest();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        quest.setCounterCount(CounterType.QUEST, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForTheGemblades());
    }
}
