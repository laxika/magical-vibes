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
        resolveAllTriggers();

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
        resolveAllTriggers();
        gs.handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(library.getFirst()).isSameAs(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom without reordering the rest")
    void scryCanPutCardOnBottom() {
        Card top = new SilverRaven();
        Card middle = new SilverRaven();
        Card bottom = new SilverRaven();
        harness.setLibrary(player1, List.of(top, middle, bottom));
        harness.setHand(player1, List.of(new SilverRaven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(middle, bottom, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering with an empty library finishes without a scry choice")
    void emptyLibraryDoesNotRequireChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SilverRaven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Silver Raven")).isEqualTo(1);
    }

    @Test
    @DisplayName("The enters trigger still scries after Silver Raven dies")
    void triggerResolvesAfterSourceDies() {
        Card top = new SilverRaven();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new SilverRaven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Silver Raven").setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(countPermanents(player1, "Silver Raven")).isZero();
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

}
