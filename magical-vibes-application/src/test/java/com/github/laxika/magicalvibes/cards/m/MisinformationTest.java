package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MisinformationTest extends BaseCardTest {

    @Test
    @DisplayName("At most three cards may be chosen")
    void choiceIsCappedAtThree() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GiantSpider(), new LightningBolt(), new HolyDay()));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only cards in an opponent's graveyard are legal targets")
    void onlyOpponentGraveyardCardsAreLegalTargets() {
        Card opponentCard = new GiantSpider();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(opponentCard.getId());
    }

    @Test
    @DisplayName("Chosen cards move to the top of the opponent's library, last chosen on top")
    void chosenCardsGoOnTopOfOpponentLibrary() {
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bears, bolt));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), bolt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactly(bolt.getId(), bears.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Misinformation");
    }

    @Test
    @DisplayName("With an empty opponent graveyard the spell resolves doing nothing")
    void noOpponentGraveyardCardsResolvesWithNoEffect() {
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HolyDay()));
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player1, "Misinformation");
    }
}
