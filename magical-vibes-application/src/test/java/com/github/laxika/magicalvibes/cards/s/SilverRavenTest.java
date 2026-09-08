package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SilverRaven.class)
class SilverRavenTest extends BaseCardTest {

    @Test
    @DisplayName("Silver Raven's enters-the-battlefield ability starts scry 1")
    void entersBattlefieldTriggersScryOne() {
        harness.setHand(player1, List.of(new SilverRaven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 can keep the card on top")
    void scryCanKeepCardOnTop() {
        harness.setHand(player1, List.of(new SilverRaven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        Card topCard = library.getFirst();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(library.getFirst()).isSameAs(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
