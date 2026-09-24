package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheOneRing.class, GrizzlyBears.class, Shock.class})
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
        harness.addToBattlefield(player1, new TheOneRing());
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
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playersWithProtectionFromEverythingUntilNextTurn).doesNotContain(player1.getId());
    }
}
