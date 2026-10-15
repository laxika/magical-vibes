package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GuardiansOfKoilos;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZahidDjinnOfTheLamp.class, ShortSword.class, AcademyDrake.class, GuardiansOfKoilos.class})
class ZahidDjinnOfTheLampTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using alternate cost: pay {3}{U} and tap an untapped artifact")
    void castWithAlternateCost() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        UUID relic = artifact.getId();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(relic));
        harness.passBothPriorities();

        // Zahid should be on battlefield
        harness.assertOnBattlefield(player1, "Zahid, Djinn of the Lamp");

        // The artifact should be tapped (not sacrificed)
        assertThat(artifact.isTapped()).isTrue();

        // Mana should be spent ({3}{U})
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be cast normally with full mana cost {4}{U}{U}")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zahid, Djinn of the Lamp");
    }

    @Test
    @DisplayName("Alternate cost fails if the artifact is already tapped")
    void alternateCostFailsWithTappedArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        UUID relic = artifact.getId();

        // Tap the artifact
        artifact.tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(relic)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Alternate cost fails if target is not an artifact")
    void alternateCostFailsWithNonArtifact() {
        UUID bears = harness.addToBattlefieldAndReturn(player1, new AcademyDrake()).getId();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bears)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails with insufficient mana")
    void alternateCostFailsWithInsufficientMana() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        UUID relic = artifact.getId();

        // Only 2 mana instead of {3}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(relic)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Alternate cost does not sacrifice the artifact")
    void alternateCostDoesNotSacrificeArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        UUID relic = artifact.getId();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(relic));
        harness.passBothPriorities();

        // Artifact should still be on battlefield (tapped, not sacrificed)
        harness.assertOnBattlefield(player1, "Short Sword");

        // Graveyard should NOT contain the artifact
        harness.assertNotInGraveyard(player1, "Short Sword");
    }

    @Test
    @DisplayName("Alternate cost works with any artifact type (equipment)")
    void alternateCostWorksWithEquipment() {
        var sword = harness.addToBattlefieldAndReturn(player1, new ShortSword());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(sword.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zahid, Djinn of the Lamp");
        assertThat(sword.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Alternate cost fails if no permanent ID is provided")
    void alternateCostFailsWithNoPermanent() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick artifact creature can pay the tap cost before Zahid resolves")
    void canTapSummoningSickArtifactCreature() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new GuardiansOfKoilos());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Zahid, Djinn of the Lamp");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Zahid, Djinn of the Lamp");
        harness.assertOnBattlefield(player1, "Guardians of Koilos");
    }

    @Test
    @DisplayName("Cannot tap an opponent's artifact to pay the alternate cost")
    void cannotTapOpponentsArtifact() {
        harness.addToBattlefield(player1, new ShortSword());
        var artifact = harness.addToBattlefieldAndReturn(player2, new ShortSword());
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Zahid, Djinn of the Lamp");
    }

    @Test
    @DisplayName("The alternate cost requires exactly one artifact")
    void cannotTapTwoArtifacts() {
        var first = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        var second = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInHand(player1, "Zahid, Djinn of the Lamp");
    }

    @Test
    @DisplayName("Normal casting leaves an available artifact untapped")
    void normalCastingDoesNotTapArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        harness.setHand(player1, List.of(new ZahidDjinnOfTheLamp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zahid, Djinn of the Lamp");
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
