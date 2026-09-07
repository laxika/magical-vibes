package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccumulateWisdom.class, AirbendingLesson.class, GrizzlyBears.class})
class AccumulateWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest on the bottom")
    void choosesOneOfTopThreeAndOrdersRest() {
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        Card top3 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2).doesNotContain(top1, top3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top3, top1);
    }

    @Test
    @DisplayName("Puts all three cards into hand with three Lessons in the graveyard")
    void putsAllThreeIntoHandWithThreeLessons() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        Card top3 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(top1, top2, top3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
