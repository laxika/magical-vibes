package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.o.OranRiefRecluse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestForPureFlame.class, BurstLightning.class, OranRiefRecluse.class})
class QuestForPureFlameTest extends BaseCardTest {

    @Test
    @DisplayName("May put a quest counter on itself when your source damages an opponent")
    void gainsQuestCounterFromDamageToOpponent() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveMayAbility(true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the damage trigger does not add a quest counter")
    void mayDeclineQuestCounter() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveMayAbility(false);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing four counters and sacrificing it doubles damage this turn")
    void activatesAndDoublesDamageThisTurn() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 4);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Quest for Pure Flame");
        harness.assertInGraveyard(player1, "Quest for Pure Flame");
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Cannot activate without four quest counters")
    void requiresFourQuestCounters() {
        addQuest().setCounterCount(CounterType.QUEST, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageToYourselfDoesNotTrigger() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsSourceDoesNotTrigger() {
        Permanent quest = addQuest();
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void damageToCreatureDoesNotTrigger() {
        Permanent quest = addQuest();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void separateDamageEventsEachAddOneCounter() {
        Permanent quest = addQuest();
        harness.setHand(player1, List.of(new BurstLightning(), new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveMayAbility(true);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveMayAbility(true);

        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    void combatDamageFromEachSourceTriggersSeparately() {
        Permanent quest = addQuest();
        for (int i = 0; i < 2; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OranRiefRecluse());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player2, 20);

        harness.passBothPriorities();
        resolveMayAbility(true);

        harness.assertLife(player2, 18);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    void doublesDamageToCreatures() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OranRiefRecluse());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Oran-Rief Recluse");
        harness.assertInGraveyard(player2, "Oran-Rief Recluse");
    }

    @Test
    void doublesDamageToYourselfButNotOpponentsSources() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 16);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 14);
    }

    @Test
    void multipleQuestsMultiplyDamage() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        addQuest().setCounterCount(CounterType.QUEST, 4);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 12);
    }

    @Test
    void damageIsNotDoubledBeforeAbilityResolves() {
        Permanent quest = addQuest();
        quest.setCounterCount(CounterType.QUEST, 5);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(quest.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Quest for Pure Flame");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.passBothPriorities();
    }

    @Test
    void doublingExpiresAtEndOfTurn() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new BurstLightning()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void doublesCombatDamage() {
        addQuest().setCounterCount(CounterType.QUEST, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OranRiefRecluse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private Permanent addQuest() {
        return harness.addToBattlefieldAndReturn(player1, new QuestForPureFlame());
    }

    private void resolveMayAbility(boolean accept) {
        int guard = 0;
        while (guard++ < 20) {
            PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(
                    PendingInteraction.MayAbilityChoice.class);
            if (choice != null) {
                harness.handleMayAbilityChosen(player1, accept);
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                return;
            }
        }
        throw new IllegalStateException("Quest trigger resolution did not finish");
    }
}
