package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Demilich.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class DemilichTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less blue for each instant or sorcery cast this turn")
    void costsOneLessBlueForEachInstantOrSorceryCastThisTurn() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getSpellsCastThisTurn(player1.getId())).hasSize(1);

        harness.setHand(player1, List.of(new Demilich()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Demilich")).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells do not reduce its cost")
    void creatureSpellsDoNotReduceItsCost() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Demilich()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking exiles an instant or sorcery and offers its copy for its normal cost")
    void attackingOffersNormalCostCopy() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new Demilich());

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(counsel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attack with no eligible graveyard card still creates a resolving trigger")
    void noEligibleGraveyardCardStillCreatesTrigger() {
        addCreatureReady(player1, new Demilich());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast from the graveyard by exiling four instants or sorceries")
    void castsFromGraveyardByExilingFourInstantsOrSorceries() {
        Demilich demilich = new Demilich();
        List<Card> exiledForCost = List.of(new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, List.of(demilich,
                exiledForCost.get(0), exiledForCost.get(1), exiledForCost.get(2), exiledForCost.get(3)));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Demilich")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(exiledForCost);
    }
}
