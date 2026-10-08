package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheMindful;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VileManifestation.class, StripedRiverwinder.class, DesertOfTheMindful.class, FeralProwler.class})
class VileManifestationTest extends BaseCardTest {

    @Test
    @DisplayName("No power boost when no cycling cards in graveyard")
    void noBoostWithoutCyclingCards() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each card with cycling in your graveyard")
    void boostsPerCyclingCard() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setGraveyard(player1, List.of(new StripedRiverwinder(), new DesertOfTheMindful()));

        // Two cycling cards -> +2/+0; toughness unaffected.
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-cycling cards in graveyard do not count")
    void ignoresNonCyclingCards() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setGraveyard(player1, List.of(new StripedRiverwinder(), new FeralProwler()));

        // Only the Striped Riverwinder has cycling.
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling cards in opponent's graveyard do not count")
    void ignoresOpponentGraveyard() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setGraveyard(player2, List.of(new StripedRiverwinder(), new DesertOfTheMindful()));

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);
    }

    @Test
    @DisplayName("Power updates dynamically as cycling cards enter the graveyard")
    void updatesDynamically() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(0);

        harness.setGraveyard(player1, List.of(new StripedRiverwinder()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new StripedRiverwinder(), new DesertOfTheMindful()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Vile Manifestation in the graveyard counts as a cycling card")
    void countsVileManifestationInGraveyard() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setGraveyard(player1, List.of(new VileManifestation()));

        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power decreases as cycling cards leave the graveyard")
    void losesBoostWhenCyclingCardsLeaveGraveyard() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setGraveyard(player1, List.of(new StripedRiverwinder(), new DesertOfTheMindful()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new DesertOfTheMindful()));
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, vile)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, vile)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cycling pays two generic mana and discards before drawing on resolution")
    void cyclingDiscardsBeforeDrawingAndImmediatelyBoostsAnotherManifestation() {
        Permanent vile = harness.addToBattlefieldAndReturn(player1, new VileManifestation());
        harness.setHand(player1, List.of(new VileManifestation()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Vile Manifestation");
        harness.assertInGraveyard(player1, "Vile Manifestation");
        harness.assertNotInHand(player1, "Feral Prowler");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Feral Prowler");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, vile)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new VileManifestation()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Vile Manifestation");
        harness.assertNotInGraveyard(player1, "Vile Manifestation");
        harness.assertNotInHand(player1, "Feral Prowler");
        assertThat(gd.stack).isEmpty();
    }
}
