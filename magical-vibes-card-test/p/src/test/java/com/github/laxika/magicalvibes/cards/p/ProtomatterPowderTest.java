package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProtomatterPowder.class, EtheriumSculptor.class, CylianElf.class})
class ProtomatterPowderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating returns the targeted artifact and sacrifices the Powder as a cost")
    void returnsArtifactFromGraveyard() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        EtheriumSculptor artifact = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player1, "Protomatter Powder");
        harness.assertInGraveyard(player1, "Protomatter Powder");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Etherium Sculptor");
        harness.assertNotInGraveyard(player1, "Etherium Sculptor");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact card when activating")
    void cannotChooseNonArtifact() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        CylianElf nonArtifact = new CylianElf();
        harness.setGraveyard(player1, List.of(nonArtifact, new EtheriumSculptor()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        EtheriumSculptor artifact = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        EtheriumSculptor artifact = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent powder = harness.addToBattlefieldAndReturn(player1, new ProtomatterPowder());
        powder.tap();
        EtheriumSculptor artifact = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsArtifact() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        EtheriumSculptor artifact = new EtheriumSculptor();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setGraveyard(player1, List.of(new EtheriumSculptor()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutTargetOrReturnItselfFromEmptyGraveyard() {
        harness.addToBattlefield(player1, new ProtomatterPowder());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Protomatter Powder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsNoncreatureArtifactChosenBeforeSacrificingSource() {
        ProtomatterPowder source = new ProtomatterPowder();
        ProtomatterPowder target = new ProtomatterPowder();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotChooseAnotherArtifactWhenTargetLeavesGraveyard() {
        ProtomatterPowder source = new ProtomatterPowder();
        EtheriumSculptor target = new EtheriumSculptor();
        ProtomatterPowder otherArtifact = new ProtomatterPowder();
        harness.addToBattlefield(player1, source);
        harness.setGraveyard(player1, List.of(target, otherArtifact));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(otherArtifact, source));
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherArtifact, source);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
