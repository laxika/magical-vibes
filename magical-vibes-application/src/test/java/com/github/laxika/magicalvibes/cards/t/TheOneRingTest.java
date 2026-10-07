package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheOneRing.class, GrizzlyBears.class, Shock.class, Commandeer.class})
class TheOneRingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting The One Ring grants protection from everything until the next turn")
    void castingGrantsProtectionFromEverything() {
        harness.setHand(player1, List.of(new TheOneRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");
    }

    @Test
    @DisplayName("The One Ring put onto the battlefield without being cast does not grant protection")
    void enteringWithoutBeingCastDoesNotGrantProtection() {
        harness.enterBattlefieldAndReturn(player1, new TheOneRing());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The One Ring adds a burden counter and draws for all burden counters")
    void activationAddsBurdenCounterAndDraws() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new TheOneRing());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.BURDEN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At upkeep, The One Ring causes life loss equal to its burden counters")
    void upkeepCausesLifeLossForBurdenCounters() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new TheOneRing());
        ring.setCounterCount(CounterType.BURDEN, 3);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The One Ring's protection ends at its controller's next turn")
    void protectionEndsAtNextTurn() {
        harness.setHand(player1, List.of(new TheOneRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Gaining control of The One Ring spell does not grant protection to a player who did not cast it")
    void stolenSpellDoesNotGrantProtection() {
        TheOneRing ring = new TheOneRing();
        harness.setHand(player1, List.of(ring));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ring.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "The One Ring");
        harness.assertNotOnBattlefield(player1, "The One Ring");
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Activation draws for existing burden counters plus the newly added counter")
    void activationDrawsForAllBurdenCounters() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new TheOneRing());
        ring.setCounterCount(CounterType.BURDEN, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.BURDEN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Upkeep life loss counts burden counters at resolution after an activation in response")
    void upkeepCountsCountersAtResolution() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new TheOneRing());
        ring.setCounterCount(CounterType.BURDEN, 1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(ring.getCounterCount(CounterType.BURDEN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The opponent's upkeep does not cause life loss from The One Ring")
    void opponentUpkeepDoesNotCauseLifeLoss() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new TheOneRing());
        ring.setCounterCount(CounterType.BURDEN, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection from The One Ring prevents combat damage during the opponent's turn")
    void protectionPreventsCombatDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TheOneRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }
}
