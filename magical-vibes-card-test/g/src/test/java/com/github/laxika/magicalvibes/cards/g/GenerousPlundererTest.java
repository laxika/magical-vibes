package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceReawakened;
import com.github.laxika.magicalvibes.cards.m.MagdaTheHoardmaster;
import com.github.laxika.magicalvibes.cards.t.TorporOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenerousPlunderer.class, TorporOrb.class, JaceReawakened.class, MagdaTheHoardmaster.class})
class GenerousPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger creates an untapped Treasure and targets only an opponent")
    void upkeepCreatesTreasuresForControllerAndOpponent() {
        addCreatureReady(player1, new GenerousPlunderer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Treasure"))
                .hasSize(1)
                .first()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isFalse());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure"))
                .hasSize(1)
                .first()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no Treasure tokens")
    void decliningUpkeepTriggerCreatesNothing() {
        addCreatureReady(player1, new GenerousPlunderer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Attacking deals damage equal to the defending player's artifact count")
    void attackDamagesDefendingPlayerForArtifactCount() {
        addCreatureReady(player1, new GenerousPlunderer());
        harness.addToBattlefield(player1, new TorporOrb());
        harness.addToBattlefield(player1, new TorporOrb());
        harness.addToBattlefield(player2, new TorporOrb());
        harness.addToBattlefield(player2, new TorporOrb());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addCreatureReady(player1, new GenerousPlunderer());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("The opponent's Treasure is created by a separate reflexive trigger")
    void opponentTreasureWaitsForReflexiveTrigger() {
        addCreatureReady(player1, new GenerousPlunderer());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
            assertThat(countPermanents(player2, "Treasure")).isZero();

            harness.handlePermanentChosen(player1, player2.getId());
            assertThat(countPermanents(player2, "Treasure")).isZero();
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
            assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Targeting the opponent with the reflexive trigger triggers crime abilities")
    void reflexiveTriggerTriggersMagda() {
        addCreatureReady(player1, new GenerousPlunderer());
        addCreatureReady(player1, new MagdaTheHoardmaster());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);

            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();

            assertThat(findPermanents(player1, "Treasure")).hasSize(2);
            assertThat(findPermanents(player1, "Treasure").stream().filter(Permanent::isTapped).count())
                    .isEqualTo(1);
            assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Attacking a planeswalker deals triggered damage to its controller")
    void attackOnPlaneswalkerDamagesDefendingPlayer() {
        addCreatureReady(player1, new GenerousPlunderer());
        harness.addToBattlefield(player2, new TorporOrb());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceReawakened());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Attack damage counts artifacts at resolution even after the source leaves")
    void attackCountsCurrentArtifactsAfterSourceLeaves() {
        Permanent plunderer = addCreatureReady(player1, new GenerousPlunderer());
        harness.addToBattlefield(player2, new TorporOrb());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(plunderer);
            gd.playerGraveyards.get(player1.getId()).add(plunderer.getCard());
            harness.addToBattlefield(player2, new TorporOrb());
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        });
    }

    @Test
    @DisplayName("The attack trigger deals no damage when the defender controls no artifacts")
    void attackWithNoDefendingArtifactsDealsNoTriggeredDamage() {
        addCreatureReady(player1, new GenerousPlunderer());
        harness.addToBattlefield(player1, new TorporOrb());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        });
    }
}
