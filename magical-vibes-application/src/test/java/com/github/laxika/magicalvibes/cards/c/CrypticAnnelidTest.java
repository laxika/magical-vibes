package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CrypticAnnelid.class, Forest.class})
class CrypticAnnelidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB scries 1, then 2, then 3")
    void etbScriesOneThenTwoThenThree() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new Forest();
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CrypticAnnelid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scryOne = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryOne.cards()).containsExactly(first);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        PendingInteraction.Scry scryTwo = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryTwo.cards()).containsExactly(second, third);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        PendingInteraction.Scry scryThree = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scryThree.cards()).containsExactly(third, fourth, fifth);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(2, 0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, third, fourth, sixth, first, second);
    }
}
