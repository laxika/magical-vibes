package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TombstoneCareerCriminal.class, DocOcksHenchmen.class, GrizzlyBears.class})
class TombstoneCareerCriminalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target Villain card from the graveyard to hand")
    void returnsTargetVillainToHand() {
        DocOcksHenchmen villain = new DocOcksHenchmen();
        harness.setGraveyard(player1, List.of(villain, new GrizzlyBears()));
        harness.setHand(player1, List.of(new TombstoneCareerCriminal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(villain.getId());

        harness.handleMultipleCardsChosen(player1, List.of(villain.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Doc Ock's Henchmen");
        harness.assertNotInGraveyard(player1, "Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("Reduces the cost of Villain spells you cast")
    void reducesVillainSpellCost() {
        harness.addToBattlefield(player1, new TombstoneCareerCriminal());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("Does not reduce the cost of non-Villain spells")
    void doesNotReduceNonVillainSpellCost() {
        harness.addToBattlefield(player1, new TombstoneCareerCriminal());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A legal Villain target must be selected for the enter ability")
    void cannotDeclineLegalVillainTarget() {
        DocOcksHenchmen villain = new DocOcksHenchmen();
        harness.setGraveyard(player1, List.of(villain));
        harness.setHand(player1, List.of(new TombstoneCareerCriminal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(villain.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("The enter ability can only target Villains in your own graveyard")
    void excludesOpponentsGraveyard() {
        DocOcksHenchmen ownVillain = new DocOcksHenchmen();
        DocOcksHenchmen opposingVillain = new DocOcksHenchmen();
        harness.setGraveyard(player1, List.of(ownVillain));
        harness.setGraveyard(player2, List.of(opposingVillain));
        harness.setHand(player1, List.of(new TombstoneCareerCriminal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownVillain.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownVillain.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Doc Ock's Henchmen");
        harness.assertInGraveyard(player2, "Doc Ock's Henchmen");
        harness.assertNotInHand(player2, "Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("Does not reduce an opponent's Villain spell cost")
    void doesNotReduceOpponentsVillainSpellCost() {
        harness.addToBattlefield(player2, new TombstoneCareerCriminal());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Villain cost reduction does not pay colored mana")
    void doesNotReduceColoredManaCost() {
        harness.addToBattlefield(player1, new TombstoneCareerCriminal());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tombstone enters normally when there are no legal graveyard targets")
    void entersWithoutLegalTargets() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new TombstoneCareerCriminal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tombstone, Career Criminal");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
