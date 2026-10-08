package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireDevils.class, CounselOfTheSoratami.class, Divination.class, GrizzlyBears.class,
        Cancel.class, TormentingVoice.class})
class WildfireDevilsTest extends BaseCardTest {

    @Test
    void etbLetsRandomlyChosenPlayerChooseTheCardToCopy() {
        Card ownDivination = new Divination();
        Card ownCounsel = new CounselOfTheSoratami();
        Card opponentDivination = new Divination();
        Card opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownDivination, ownCounsel));
        harness.setGraveyard(player2, List.of(opponentDivination, opponentCounsel));
        harness.castFromHand(player1, new WildfireDevils(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(2);
        Card chosenCard = gd.playerGraveyards.get(choice.playerId()).get(choice.validIndices().getFirst());

        harness.handleGraveyardCardChosen(
                choice.playerId().equals(player1.getId()) ? player1 : player2,
                choice.validIndices().getFirst());

        assertThat(gd.findExiledCard(chosenCard.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(choice.playerId())).doesNotContain(chosenCard);
        PendingInteraction.MayAbilityChoice mayChoice = gd.interaction.activeInteraction(
                PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());

        int handSizeBeforeCopy = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCopy + 2);
        assertThat(gd.findExiledCard(chosenCard.getId())).isNotNull();
    }

    @Test
    void upkeepTriggerAlsoCopiesAChosenInstantOrSorcery() {
        Divination divination = new Divination();
        Card opponentDivination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setGraveyard(player2, List.of(opponentDivination));
        harness.addToBattlefield(player1, new WildfireDevils());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        boolean ownCardWasExiled = gd.findExiledCard(divination.getId()) != null;
        boolean opponentCardWasExiled = gd.findExiledCard(opponentDivination.getId()) != null;
        assertThat(ownCardWasExiled ^ opponentCardWasExiled).isTrue();
    }

    @Test
    void doesNothingWhenRandomlyChosenPlayerHasNoInstantOrSorcery() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new WildfireDevils());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void decliningCopyStillExilesOriginalAndRemovesCopy() {
        Card ownCard = new Divination();
        Card opponentCard = new Divination();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new WildfireDevils());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.getPlayerExiledCards(player1.getId()).size()
                + gd.getPlayerExiledCards(player2.getId()).size()).isEqualTo(1);
        assertThat((gd.findExiledCard(ownCard.getId()) != null)
                ^ (gd.findExiledCard(opponentCard.getId()) != null)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void instantCopyWithNoLegalTargetCeasesToExist() {
        Card ownCard = new Cancel();
        Card opponentCard = new Cancel();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new WildfireDevils());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()).size()
                + gd.getPlayerExiledCards(player2.getId()).size()).isEqualTo(1);
        assertThat((gd.findExiledCard(ownCard.getId()) != null)
                ^ (gd.findExiledCard(opponentCard.getId()) != null)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.setGraveyard(player2, List.of(new Divination()));
        harness.addToBattlefield(player1, new WildfireDevils());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canPayMandatoryDiscardCostToCastCopy() {
        harness.setGraveyard(player1, List.of(new TormentingVoice()));
        harness.setGraveyard(player2, List.of(new TormentingVoice()));
        Card discardCard = new GrizzlyBears();
        Card retainedCard = new CounselOfTheSoratami();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(discardCard, retainedCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addToBattlefield(player1, new WildfireDevils());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardCard, retainedCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()).size()
                + gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(2);
    }
}
