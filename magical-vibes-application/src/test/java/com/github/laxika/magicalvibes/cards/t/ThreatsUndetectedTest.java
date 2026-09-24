package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreatsUndetected.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class})
class ThreatsUndetectedTest extends BaseCardTest {

    @Test
    void searchesCreaturesWithDifferentPowersAndShufflesChosenCardsBack() {
        Card onePower = new LlanowarElves();
        Card duplicatePower = new LlanowarElves();
        Card twoPower = new GrizzlyBears();
        Card threePower = new HillGiant();
        Card fourPower = new AirElemental();
        Card nonCreature = new Forest();
        harness.setLibrary(player1, List.of(onePower, duplicatePower, twoPower, threePower, fourPower, nonCreature));
        harness.setHand(player1, List.of(new ThreatsUndetected()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(offeredCards()).containsExactlyInAnyOrder(onePower, duplicatePower, twoPower,
                threePower, fourPower);
        pick(onePower);
        assertThat(offeredCards()).doesNotContain(duplicatePower);

        pick(twoPower);
        pick(threePower);
        pick(fourPower);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(onePower.getId(), threePower.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(twoPower, fourPower);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(duplicatePower, nonCreature, onePower, threePower);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Threats Undetected");
    }

    private List<Card> offeredCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
    }

    private void pick(Card card) {
        int index = offeredCards().indexOf(card);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }
}
