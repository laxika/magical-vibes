package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubbleReading.class, Mountain.class, AxebaneBeast.class})
class RubbleReadingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and starts scry 2")
    void destroysTargetLandAndStartsScryTwo() {
        harness.addToBattlefield(player2, new Mountain());
        castRubbleReading(harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        PendingInteraction.Scry interaction = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 can put both cards on the bottom")
    void scryTwoCanPutBothCardsOnBottom() {
        harness.addToBattlefield(player2, new Mountain());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalFirst = deck.get(0);
        Card originalSecond = deck.get(1);

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(originalSecond);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalFirst);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AxebaneBeast());
        harness.setHand(player1, List.of(new RubbleReading()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Axebane Beast")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own land and scry your own library")
    void destroysOwnLand() {
        harness.addToBattlefield(player1, new Mountain());
        castRubbleReading(harness.getPermanentId(player1, "Mountain"));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("Scry can keep both cards on top in reverse order")
    void keepsBothCardsOnTopInChosenOrder() {
        Card first = new Mountain();
        Card second = new AxebaneBeast();
        Card third = new RubbleReading();
        harness.setLibrary(player1, List.of(first, second, third));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.addToBattlefield(player2, new Mountain());

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("Scry can split the cards between top and bottom")
    void splitsCardsBetweenTopAndBottom() {
        Card first = new Mountain();
        Card second = new AxebaneBeast();
        Card third = new RubbleReading();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player2, new Mountain());

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("Scry 2 with one card looks at only that card")
    void scriesWithOneCardLibrary() {
        Card onlyCard = new Mountain();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addToBattlefield(player2, new Mountain());

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("An empty library does not prevent land destruction or spell completion")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new Mountain());

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Rubble Reading");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not scry when the only target has left the battlefield")
    void doesNotScryWithMissingTarget() {
        harness.addToBattlefield(player2, new Mountain());
        java.util.UUID targetId = harness.getPermanentId(player2, "Mountain");
        List<Card> library = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.setHand(player1, List.of(new RubbleReading()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    private void castRubbleReading(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RubbleReading()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
