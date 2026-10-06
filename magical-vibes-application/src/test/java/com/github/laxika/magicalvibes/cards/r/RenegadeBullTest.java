package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Blaze;
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

@CardUsed({RenegadeBull.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class, Blaze.class})
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

        declareAttackers(List.of(0));

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

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingInstantBoostsBullBeforeSpellResolves() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(1);
        assertThat(bull.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void chosenXCountsTowardCastSpellManaValue() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(5);
        assertThat(bull.getToughnessModifier()).isZero();
    }

    @Test
    void creatureSpellDoesNotBoostBull() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(bull.getPowerModifier()).isZero();
    }

    @Test
    void opponentsInstantDoesNotBoostBull() {
        Permanent bull = addCreatureReady(player1, new RenegadeBull());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(bull.getPowerModifier()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void copiedInstantCanChooseTargetAndTriggersCastBoost() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent bull = addCreatureReady(player1, new RenegadeBull());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(bull.getPowerModifier()).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningCopyStillExilesOriginalCard() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        addCreatureReady(player1, new RenegadeBull());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(counsel);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedGraveyardTargetIsNotCopied() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        addCreatureReady(player1, new RenegadeBull());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(counsel));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(counsel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayChooseNoTargetWithEligibleCardInGraveyard() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        addCreatureReady(player1, new RenegadeBull());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(counsel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
