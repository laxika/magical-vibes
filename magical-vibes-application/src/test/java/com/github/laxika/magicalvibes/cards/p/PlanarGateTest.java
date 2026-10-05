package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackManaBattery.class, BronzeHorse.class, CatWarriors.class, PlanarGate.class})
class PlanarGateTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells you cast cost {2} less")
    void creatureSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new PlanarGate());
        harness.castFromHand(player1, new CatWarriors(), "{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CatWarriors.class);
    }

    @Test
    @DisplayName("Noncreature spells are not reduced")
    void noncreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        assertThatThrownBy(() -> harness.castFromHand(player1, new BlackManaBattery(), "{2}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creature spells are reduced")
    void artifactCreatureSpellsAreReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        harness.castFromHand(player1, new BronzeHorse(), "{5}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeHorse.class);
    }

    @Test
    @DisplayName("Planar Gate does not reduce an opponent's creature spells")
    void opponentCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        assertThatThrownBy(() -> harness.castFromHand(player2, new CatWarriors(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Planar Gates reduce creature spells by four generic mana")
    void multipleGatesStack() {
        harness.addToBattlefield(player1, new PlanarGate());
        harness.addToBattlefield(player1, new PlanarGate());

        harness.castFromHand(player1, new BronzeHorse(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeHorse.class);
    }

    @Test
    @DisplayName("Excess generic reduction does not pay colored mana")
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new PlanarGate());
        harness.addToBattlefield(player1, new PlanarGate());

        assertThatThrownBy(() -> harness.castFromHand(player1, new CatWarriors(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enough Planar Gates can reduce a generic creature cost to zero")
    void genericCostCannotFallBelowZero() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PlanarGate());
        }

        harness.castFromHand(player1, new BronzeHorse(), "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeHorse.class);
    }

    @Test
    @DisplayName("Tapped Planar Gate still reduces creature costs")
    void tappedGateStillReducesCosts() {
        harness.addToBattlefieldAndReturn(player1, new PlanarGate()).setTapped(true);

        harness.castFromHand(player1, new BronzeHorse(), "{5}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeHorse.class);
    }

    @Test
    @DisplayName("Planar Gate stops reducing costs after leaving the battlefield")
    void reductionEndsWhenGateLeavesBattlefield() {
        var gate = harness.addToBattlefieldAndReturn(player1, new PlanarGate());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gate));

        assertThatThrownBy(() -> harness.castFromHand(player1, new BronzeHorse(), "{5}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
