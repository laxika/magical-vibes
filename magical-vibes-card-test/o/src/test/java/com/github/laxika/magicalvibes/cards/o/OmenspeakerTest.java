package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Omenspeaker.class, Island.class, Disperse.class})
class OmenspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Omenspeaker enters a scry 2")
    void etbEntersScryTwo() {
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Omenspeaker");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 can reorder both cards on top")
    void scryReordersTop() {
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck.get(0)).isSameAs(top1);
        assertThat(deck.get(1)).isSameAs(top0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 can put both cards on the bottom")
    void scryBothToBottom() {
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        int size = deck.size();
        assertThat(deck.get(0)).isNotSameAs(top0);
        assertThat(deck.get(size - 2)).isSameAs(top0);
        assertThat(deck.get(size - 1)).isSameAs(top1);
    }

    @Test
    @DisplayName("Scry 2 can split one card top and one bottom")
    void scrySplit() {
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(deck.get(0)).isSameAs(top1);
        assertThat(deck.getLast()).isSameAs(top0);
    }

    @Test
    @DisplayName("Scry 2 can reverse both cards on the bottom without disturbing the rest")
    void scryReordersBottom() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with a one-card library looks at only that card")
    void scryWithOneCard() {
        Card onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with an empty library finishes without a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Omenspeaker");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The scry trigger resolves for its controller after Omenspeaker leaves")
    void scryAfterSourceLeaves() {
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new Omenspeaker()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Omenspeaker"));
        harness.assertNotOnBattlefield(player1, "Omenspeaker");
        harness.assertInHand(player1, "Omenspeaker");
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }
}
