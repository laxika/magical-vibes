package com.github.laxika.magicalvibes.service;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class CommanderGameTest extends BaseCardTest {
    private Card commander() {
        Card card = new Card(); card.setName("Test Commander"); card.setType(CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY)); card.setManaCost("{1}"); card.setPower(2); card.setToughness(2);
        card.setOwnerId(player1.getId()); card.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(card)));
        gd.currentStep = TurnStep.PRECOMBAT_MAIN; gd.activePlayerId = player1.getId(); gd.priorityPassedBy.clear();
        return card;
    }
    private void cast(Card card) {
        gs.castCommander(gd, player1, card.getId(), () -> gs.playCard(gd, player1, 0, null, null, null));
    }
    @Test void castsFromCommandWithoutChangingHandAndIncreasesTax() {
        Card card = commander();
        var hand = List.copyOf(gd.playerHands.get(player1.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        cast(card);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(card.getId()) && entry.getSourceZone() == Zone.COMMAND);
        assertThat(gd.commanderTaxByCardId.get(card.getId())).isEqualTo(2);
        assertThat(gd.commandCastCardId).isNull();
    }
    @Test void failedCastDoesNotSpendTaxOrRemoveCommander() {
        Card card = commander();
        gd.commanderTaxByCardId.put(card.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> cast(card)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(card);
        assertThat(gd.commanderTaxByCardId.get(card.getId())).isEqualTo(2);
        assertThat(gd.commandCastCardId).isNull();
    }
    @Test void graveyardReturnMayBeDeclinedAndIsNotOfferedAgain() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.runStateBasedActions();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommanderReturnChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test void graveyardReturnPreservesTax() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        gd.commanderTaxByCardId.put(card.getId(), 4);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.runStateBasedActions(); harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(card);
        assertThat(gd.commanderTaxByCardId.get(card.getId())).isEqualTo(4);
    }
    @Test void commanderDamageLossIgnoresLifeAndSimulationMapsAreIndependent() {
        Card card = commander();
        gd.playerLifeTotals.put(player2.getId(), 100);
        gd.commanderDamageReceived.put(player2.getId(), new HashMap<>(Map.of(card.getId(), 20)));
        GameData copy = gd.simulationCopy();
        copy.commanderDamageReceived.get(player2.getId()).put(card.getId(), 21);
        assertThat(gd.commanderDamageReceived.get(player2.getId()).get(card.getId())).isEqualTo(20);
        harness.runStateBasedActions(); assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        gd.commanderDamageReceived.get(player2.getId()).put(card.getId(), 21);
        harness.runStateBasedActions(); assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test void handReplacementLetsOwnerKeepCommanderInHand() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        gd.addCardToHand(player1.getId(), card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        harness.runStateBasedActions();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.pendingCommanderZoneMoves).isEmpty();
    }
    @Test void libraryReplacementLetsOwnerReturnCommanderToCommandZone() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addFirst(card);
        harness.runStateBasedActions(); harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(card);
    }
    @Test void bouncingCommanderDoesNotCountAsReturnedToHandWhenReplaced() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.runStateBasedActions(); harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(card);
        assertThat(gd.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn).doesNotContain(player1.getId());
    }
    @Test void combatDamageTracksCommanderIdentity() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setAttacking(true); permanent.setSummoningSick(false);
        gd.playerLifeTotals.put(player2.getId(), 40);
        harness.resolveCombatDamage();
        assertThat(gd.commanderDamageReceived.get(player2.getId()).get(card.getId())).isEqualTo(2);
    }

    @Test void commanderSetupUsesFortyLifeAndNinetyNineCardLibrary() {
        var setup = harness.getGameSetupService();
        Player creator = new Player(UUID.randomUUID(), "Commander creator");
        Player joiner = new Player(UUID.randomUUID(), "Commander joiner");
        GameData game = setup.createGame("Commander setup", creator, "10e-white-theme-deck", false);
        game.format = DeckFormat.COMMANDER;
        Card first = new Card(); first.setName("First commander"); first.setType(CardType.CREATURE);
        Card second = new Card(); second.setName("Second commander"); second.setType(CardType.CREATURE);
        List<Card> firstDeck = new ArrayList<>(), secondDeck = new ArrayList<>();
        for (int i = 0; i < 99; i++) { firstDeck.add(new Card()); secondDeck.add(new Card()); }
        game.acceptedDecks.put(creator.getId(), new DeckDefinition(firstDeck, List.of(), first));
        setup.joinGame(game, joiner, "10e-white-theme-deck", new DeckDefinition(secondDeck, List.of(), second));
        for (Player player : List.of(creator, joiner)) {
            assertThat(game.getLife(player.getId())).isEqualTo(40);
            assertThat(game.playerHands.get(player.getId())).hasSize(7);
            assertThat(game.playerDecks.get(player.getId())).hasSize(92);
            assertThat(game.playerCommandZones.get(player.getId())).hasSize(1);
            assertThat(game.startingDeckSizes.get(player.getId())).isEqualTo(100);
        }
        harness.getGameRegistry().remove(game.id);
    }

    @Test void recastPaysTwoAdditionalMana() {
        Card card = commander(); harness.addMana(player1, ManaColor.COLORLESS, 1); cast(card);
        gd.stack.clear(); gd.playerCommandZones.get(player1.getId()).add(card); gd.priorityPassedBy.clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3); cast(card);
        assertThat(gd.commanderTaxByCardId.get(card.getId())).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
    @Test void preventedCombatDamageDoesNotCount() {
        Card card = commander(); gd.playerCommandZones.get(player1.getId()).clear();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setAttacking(true); permanent.setSummoningSick(false);
        gd.preventAllCombatDamageToPlayers = true;
        harness.resolveCombatDamage();
        assertThat(gd.commanderDamageReceived.getOrDefault(player2.getId(), Map.of()).getOrDefault(card.getId(), 0)).isZero();
    }
    @Test void copyingACommanderDoesNotCopyItsDesignation() {
        Card commander = commander();
        Card body = new Card(); body.setName("Copy"); body.setType(CardType.CREATURE); body.setPower(2); body.setToughness(2);
        Permanent copy = harness.addToBattlefieldAndReturn(player1, body); copy.setCard(commander);
        copy.setAttacking(true); copy.setSummoningSick(false);
        harness.resolveCombatDamage();
        assertThat(gd.commanderDamageReceived.getOrDefault(player2.getId(), Map.of())).isEmpty();
    }
}
