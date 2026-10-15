package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninSurveyor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RidesEnd.class, LeoninSurveyor.class, AirResponseUnit.class, Forest.class})
class RidesEndTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a tapped creature for the reduced cost")
    void exilesTappedCreatureForReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSurveyor());
        target.tap();

        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles an untapped creature for the full cost")
    void exilesUntappedCreatureForFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSurveyor());

        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles a Vehicle target")
    void exilesVehicleTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        target.tap();

        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void exilesOwnTappedCreatureWithOneWhiteAndOneColorless() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninSurveyor());
        target.tap();
        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void tappedUnrelatedPermanentDoesNotReduceCostForUntappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSurveyor());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        unrelated.tap();
        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void reductionDoesNotRemoveWhiteManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSurveyor());
        target.tap();
        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void untappingTargetAfterCastingDoesNotPreventExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSurveyor());
        target.tap();
        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void exilesUntappedUncrewedVehicleForFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setHand(player1, java.util.List.of(new RidesEnd()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }
}
