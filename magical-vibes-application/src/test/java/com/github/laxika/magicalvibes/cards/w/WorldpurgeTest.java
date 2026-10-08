package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Worldpurge.class, GrizzlyBears.class, LlanowarElves.class, Plains.class})
class WorldpurgeTest extends BaseCardTest {

    private void keepEntireHand(com.github.laxika.magicalvibes.model.Player player) {
        List<UUID> handIds = gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
        harness.handleMultipleCardsChosen(player, handIds);
    }

    @Test
    @DisplayName("Returns all permanents to their owners' hands; kept cards stay in hand")
    void returnsAllPermanentsAndPlayersKeepHands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Worldpurge()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());

        // Active player (player1) chooses first.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.KeepCardsInHandChoice.class);
        harness.clearMessages();
        keepEntireHand(player1);
        List<String> messages = harness.getConn2().getSentMessages();
        int promptIndex = firstMessageContaining(messages, "\"type\":\"INTERACTION_PROMPT\"");
        assertThat(promptIndex).isPositive();
        assertThat(messages.subList(0, promptIndex))
                .anyMatch(message -> message.contains("\"type\":\"GAME_STATE\""));
        keepEntireHand(player2);

        // Both creatures are back in their owners' hands; battlefields are empty.
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards not kept are shuffled into the owner's library")
    void unkeptCardsAreShuffledIntoLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card kept = new GrizzlyBears();
        Card shuffled = new LlanowarElves();
        harness.setHand(player1, List.of(new Worldpurge(), kept, shuffled));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());

        // Keep only the Grizzly Bears; the Llanowar Elves goes into the library.
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shuffled);
    }

    @Test
    @DisplayName("Keeping zero cards shuffles the whole hand into the library")
    void keepingZeroShufflesWholeHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card a = new GrizzlyBears();
        Card b = new LlanowarElves();
        harness.setHand(player1, List.of(new Worldpurge(), a, b));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(a, b);
    }

    @Test
    @DisplayName("Each player loses all unspent mana")
    void eachPlayerLosesUnspentMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Worldpurge()));
        harness.setHand(player2, List.of());
        // 8 to pay the spell + 2 left floating; player2 has floating mana too.
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        // No player has cards to keep, so resolution runs straight through to the mana-loss step.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Returned lands count toward the seven-card limit and mana is lost after both choices")
    void returnedLandsCountTowardLimitAndManaLossResumes() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card returnedLand = new Plains();
        harness.addToBattlefield(player1, returnedLand);
        List<Card> hand = new ArrayList<>();
        hand.add(new Worldpurge());
        for (int i = 0; i < 7; i++) {
            hand.add(new Plains());
        }
        harness.setHand(player1, hand);
        Card opposingCard = new Plains();
        harness.setHand(player2, List.of(opposingCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        List<Card> returnedHand = new ArrayList<>(gd.playerHands.get(player1.getId()));
        assertThat(returnedHand).hasSize(8).contains(returnedLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                returnedHand.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        List<Card> kept = returnedHand.subList(1, 8);
        harness.handleMultipleCardsChosen(player1, kept.stream().map(Card::getId).toList());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(3);
        harness.handleMultipleCardsChosen(player2, List.of(opposingCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(returnedHand.getFirst());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("All players choose before any unkept cards are shuffled")
    void shufflingWaitsUntilAllPlayersChoose() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card firstCard = new Plains();
        Card secondCard = new Plains();
        harness.setHand(player1, List.of(new Worldpurge(), firstCard));
        harness.setHand(player2, List.of(secondCard));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("A land controlled by an opponent returns to its owner's hand")
    void returnsOpponentControlledLandToOwner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card land = new Plains();
        land.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, land);
        gd.stolenCreatures.put(permanent.getId(), player1.getId());
        harness.setHand(player1, List.of(new Worldpurge()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, List.of());
        keepEntireHand(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private static int firstMessageContaining(List<String> messages, String value) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).contains(value)) {
                return i;
            }
        }
        return -1;
    }
}
