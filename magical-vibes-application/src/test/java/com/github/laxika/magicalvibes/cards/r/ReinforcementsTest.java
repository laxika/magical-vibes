package com.github.laxika.magicalvibes.cards.r;

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

class ReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("At most three creature cards may be chosen")
    void choiceIsCappedAtThree() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GiantSpider(), new GiantSpider()));
        harness.setHand(player1, List.of(new Reinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Fewer creature cards than three caps the choice at what is available")
    void choiceIsCappedAtAvailableCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.setHand(player1, List.of(new Reinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen creature cards move from the graveyard to the top of the library")
    void chosenCreaturesGoOnTopOfLibrary() {
        Card bears = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bears, spider));
        harness.setHand(player1, List.of(new Reinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), spider.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), spider.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Reinforcements");
    }

    @Test
    @DisplayName("Only creature cards in your own graveyard are legal targets")
    void onlyOwnCreatureCardsAreLegalTargets() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GiantSpider()));
        harness.setHand(player1, List.of(new Reinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("With no creature cards in the graveyard the spell resolves doing nothing")
    void noCreatureCardsResolvesWithNoEffect() {
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HolyDay()));
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new Reinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player1, "Reinforcements");
    }
}
