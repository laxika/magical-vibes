package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FabricationFoundry.class, ChromaticStar.class, GrizzlyBears.class,
        MindStone.class, Ornithopter.class, PropheticPrism.class})
class FabricationFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability produces mana restricted to artifacts")
    void manaAbilityRestrictsManaToArtifacts() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChromaticStar()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.WHITE)).isEqualTo(1);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifact() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling a non-empty artifact subset sets X and returns an artifact")
    void exilesSelectedArtifactsAndReturnsTarget() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Card target = new PropheticPrism();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("At least one artifact");

        harness.handleMultiplePermanentsChosen(player1, List.of(mindStone.getId(), ornithopter.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(mindStone.getCard().getId(), ornithopter.getCard().getId());
        harness.assertOnBattlefield(player1, "Prophetic Prism");
    }

    @Test
    void restrictedManaPaysForArtifactAbility() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.WHITE)).isZero();
    }

    @Test
    void zeroManaValueArtifactCanPayCostAndReturnZeroManaValueTarget() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.handleMultiplePermanentsChosen(player1, List.of(material.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(material.getCard().getId());
    }

    @Test
    void selectedCostMustCoverTargetManaValue() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        Permanent star = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Card target = new MindStone();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(star.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(stone.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chromatic Star");
        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void cannotExileFoundryOrOpponentsArtifact() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new FabricationFoundry());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(foundry.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(material.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fabrication Foundry");
        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    void returnAbilityCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.addToBattlefield(player1, new MindStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void cannotReturnArtifactFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.addToBattlefield(player1, new MindStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void cannotReturnNonartifactCard() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.addToBattlefield(player1, new MindStone());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
