package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SickeningShoal.class, GnarledMass.class, TendoIceBridge.class})
class SickeningShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -X/-X and dies when toughness drops to zero")
    void killsCreatureWithMatchingX() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstant(player1, 0, 3, massId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("A creature that survives keeps the -X/-X until end of turn")
    void shrinksSurvivingCreature() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstant(player1, 0, 2, massId);
        harness.passBothPriorities();

        Permanent mass = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(mass.getEffectivePower()).isEqualTo(1);
        assertThat(mass.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -X/-X wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstant(player1, 0, 2, massId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mass = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(mass.getEffectivePower()).isEqualTo(3);
        assertThat(mass.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiling a black card with mana value X pays the alternative cost")
    void alternativeCostExilesBlackCardWithManaValueX() {
        harness.addToBattlefield(player2, new GnarledMass());
        // Sickening Shoal's own mana value is 2, so exiling one pays for X = 2 with no mana spent.
        harness.setHand(player1, List.of(new SickeningShoal(), new SickeningShoal()));

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstantWithAlternateExileFromHand(player1, 0, 2, massId, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarled Mass");
        Permanent mass = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(mass.getEffectivePower()).isEqualTo(1);
        assertThat(mass.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal(), new SickeningShoal()));

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 3, massId, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The alternative cost rejects a nonblack card even when its mana value matches X")
    void alternativeCostRejectsNonblackCard() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal(), new GnarledMass()));

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 3, massId, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 leaves the target unchanged")
    void zeroXLeavesTargetUnchanged() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID massId = harness.getPermanentId(player2, "Gnarled Mass");
        harness.castInstant(player1, 0, 0, massId);
        harness.passBothPriorities();

        Permanent mass = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(mass.getEffectivePower()).isEqualTo(3);
        assertThat(mass.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID landId = harness.getPermanentId(player1, "Tendo Ice Bridge");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
