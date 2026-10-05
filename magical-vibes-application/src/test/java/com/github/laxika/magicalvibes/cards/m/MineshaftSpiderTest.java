package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineshaftSpider.class, Forest.class, MinersGuidewing.class})
class MineshaftSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may prompt")
    void enteringTheBattlefieldCreatesMayPrompt() {
        setupAndCast();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability mills two cards")
    void acceptingMayMillsTwoCards() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(deck).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not mill")
    void decliningMayDoesNotMill() {
        setupAndCast();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mills the top two cards of only its controller's library")
    void millsTopTwoCardsOfControllersLibrary() {
        Card first = new Forest();
        Card second = new MineshaftSpider();
        Card third = new Forest();
        Card opponentsCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        setupAndCast();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card left, mills that card")
    void millsOnlyRemainingCard() {
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));
        harness.setGraveyard(player1, List.of());
        setupAndCast();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting mill with an empty library finishes without moving cards")
    void emptyLibraryFinishesResolution() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        setupAndCast();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reach allows blocking a flying creature")
    void canBlockFlyingCreature() {
        Permanent spider = addCreatureReady(player2, new MineshaftSpider());
        addCreatureReady(player1, new MinersGuidewing());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new MineshaftSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
    }
}
