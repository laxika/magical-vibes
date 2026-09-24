package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeBull.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class RenegadeBullTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery gives Renegade Bull +X/+0 for its mana value")
    void castingInstantOrSorceryBoostsByManaValue() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(3);
        assertThat(bull.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The spell-cast boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(bull.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking exiles an own instant or sorcery and offers its copy for free")
    void attackingOffersFreeCopyOfOwnInstantOrSorcery() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel, new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new RenegadeBull());

        declareAttackers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(counsel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attack with no eligible graveyard card resolves without a target")
    void noEligibleGraveyardCardDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new RenegadeBull());

        declareAttackers();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void declareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
