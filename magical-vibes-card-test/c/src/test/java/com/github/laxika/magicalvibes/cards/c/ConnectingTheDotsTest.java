package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConnectingTheDots.class, GrizzlyBears.class})
class ConnectingTheDotsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one face-down card for each creature that attacks")
    void exilesTopCardForEachAttackingCreature() {
        Permanent source = castConnectingTheDots();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveAllTriggers();

        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> source.getId().equals(entry.sourcePermanentId()))
                .toList();
        assertThat(exiled).hasSize(2).allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding and sacrificing returns all exiled cards to their owners' hands")
    void activationReturnsExiledCardsToOwnersHands() {
        Permanent source = castConnectingTheDots();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveAllTriggers();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Connecting the Dots");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Neither player can look at the face-down exiled cards")
    void faceDownCardsAreHiddenFromBothPlayers() throws Exception {
        Permanent source = castConnectingTheDots();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();
        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            String message = connection.getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
            GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
            var sourceView = state.battlefields().stream().flatMap(List::stream)
                    .filter(permanent -> permanent.id().equals(source.getId())).findFirst().orElseThrow();
            assertThat(sourceView.faceDownExiledCards()).isEmpty();
            assertThat(sourceView.faceDownExiledCount()).isEqualTo(1);
            assertThat(state.lookedAtExileCards()).isEmpty();
        }
    }

    @Test
    @DisplayName("An empty hand can pay the discard-hand activation cost")
    void activationWorksWithEmptyHand() {
        Permanent source = castConnectingTheDots();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Connecting the Dots");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(exiledCard);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each copy returns only the cards exiled with that copy")
    void separateCopiesKeepSeparateExiledCards() {
        Permanent firstSource = castConnectingTheDots();
        harness.castFromHand(player1, new ConnectingTheDots(), "{1}{R}");
        harness.passBothPriorities();
        Permanent secondSource = findPermanents(player1, "Connecting the Dots").getLast();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();
        List<Card> firstCards = List.copyOf(gd.getCardsExiledByPermanent(firstSource.getId()));
        List<Card> secondCards = List.copyOf(gd.getCardsExiledByPermanent(secondSource.getId()));
        assertThat(firstCards).hasSize(1);
        assertThat(secondCards).hasSize(1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstSource), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(firstCards);
        assertThat(gd.getCardsExiledByPermanent(secondSource.getId())).containsExactlyElementsOf(secondCards);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondSource);
    }

    @Test
    @DisplayName("Opposing creatures attacking do not trigger the enchantment")
    void opposingAttackersDoNotExileCards() {
        Permanent source = castConnectingTheDots();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking with an empty library does not exile a card or cause a draw loss")
    void emptyLibraryDoesNotCauseDrawLoss() {
        Permanent source = castConnectingTheDots();
        harness.setLibrary(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An attack trigger still exiles its card after the enchantment is sacrificed")
    void pendingAttackTriggerResolvesAfterSacrifice() {
        Permanent source = castConnectingTheDots();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Connecting the Dots");
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent castConnectingTheDots() {
        harness.castFromHand(player1, new ConnectingTheDots(), "{1}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Connecting the Dots");
    }
}
