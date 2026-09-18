package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.b.BurningWish;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.t.Tazeem;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.f.Fork;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@CardUsed({Shahrazad.class, Plains.class, BurningWish.class, Counterspell.class})
class ShahrazadTest extends BaseCardTest {
    private List<Card> library(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(new Plains());
        return cards;
    }

    private GameData cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shahrazad()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        return gd.session.active();
    }

    @Test
    void playsSubgameAndResumesWithRoundedLifeLoss() {
        List<Card> first = library(20);
        List<Card> second = library(20);
        harness.setLibrary(player1, first);
        harness.setLibrary(player2, second);
        harness.setLife(player2, 19);
        GameData child = cast();
        assertThat(child).isNotSameAs(gd);
        assertThat(child.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(child.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c instanceof Shahrazad);
        gs.surrender(child, player2);
        assertThat(gd.session.active()).isSameAs(gd);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c instanceof Shahrazad);
    }

    @Test
    void shortLibrariesDrawTheSubgameAndBothLoseLife() {
        harness.setLibrary(player1, library(3));
        harness.setLibrary(player2, library(4));
        GameData child = cast();
        gs.keepHand(child, player1);
        gs.keepHand(child, player2);
        assertThat(gd.session.active()).isSameAs(gd);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void bothConnectionsReceivePrivateBoardsAndStayInTheSessionAfterAChildEnds() {
        harness.setLibrary(player1, library(20));
        harness.setLibrary(player2, library(20));
        GameData child = cast();
        var mapper = new com.github.laxika.magicalvibes.service.JacksonConfig().objectMapper();
        var connections = List.of(harness.getConn1(), harness.getConn2());
        var players = List.of(player1, player2);
        for (int i = 0; i < connections.size(); i++) {
            var messages = connections.get(i).getMessagesContaining("ACTIVE_GAME_CHANGED");
            assertThat(messages).hasSize(1);
            var message = mapper.readTree(messages.getFirst());
            assertThat(message.get("context").get("sessionId").asText()).isEqualTo(gd.id.toString());
            assertThat(message.get("context").get("activeGameId").asText()).isEqualTo(child.id.toString());
            assertThat(message.get("depth").asInt()).isEqualTo(1);
            var ownHand = child.playerHands.get(players.get(i).getId()).stream().map(card -> card.getId().toString()).toList();
            List<String> projectedHand = new ArrayList<>();
            message.get("game").get("hand").forEach(card -> projectedHand.add(card.get("id").asText()));
            assertThat(projectedHand).containsExactlyElementsOf(ownHand);
        }
        harness.clearMessages();
        gs.surrender(child, player2);
        for (var connection : connections) {
            assertThat(connection.getMessagesContaining("GAME_OVER")).isEmpty();
            var message = mapper.readTree(connection.getMessagesContaining("ACTIVE_GAME_CHANGED").getFirst());
            assertThat(message.get("context").get("activeGameId").asText()).isEqualTo(gd.id.toString());
            assertThat(message.get("context").get("activationEpoch").asLong()).isEqualTo(2);
            assertThat(message.get("depth").asInt()).isZero();
        }
    }

    private void keep(GameData game) {
        gs.keepHand(game, player1);
        gs.keepHand(game, player2);
    }

    private void castIn(GameData game, Card card, ManaColor color, int mana) {
        game.activePlayerId = player1.getId();
        game.currentStep = TurnStep.PRECOMBAT_MAIN;
        game.priorityPassedBy.clear();
        game.playerHands.get(player1.getId()).addFirst(card);
        for (int i = 0; i < mana; i++) game.playerManaPools.get(player1.getId()).add(color);
        gs.playCard(game, player1, 0, 0, null, null);
        for (int i = 0; i < 2 && game.session.active() == game && !game.stack.isEmpty(); i++) {
            UUID priority = gqs.getPriorityPlayerId(game);
            gs.passPriority(game, priority.equals(player1.getId()) ? player1 : player2);
        }
    }

    @Test
    void fiveNestedGamesReturnOneLevelAtATime() {
        harness.setLibrary(player1, library(80));
        harness.setLibrary(player2, library(80));
        GameData child = cast();
        List<GameData> parents = new ArrayList<>();
        parents.add(gd);
        for (int depth = 1; depth < 5; depth++) {
            keep(child);
            parents.add(child);
            castIn(child, new Shahrazad(), ManaColor.WHITE, 2);
            child = gd.session.active();
            assertThat(gd.session.depth()).isEqualTo(depth + 1);
        }
        for (int depth = 5; depth > 0; depth--) {
            gs.surrender(gd.session.active(), player2);
            GameData resumed = parents.get(depth - 1);
            assertThat(gd.session.active()).isSameAs(resumed);
            assertThat(resumed.getLife(player2.getId())).isEqualTo(10);
            assertThat(gd.session.depth()).isEqualTo(depth - 1);
        }
    }

    @Test
    void simulationCopiesEverySuspendedFrameWithoutSharingMutableState() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        GameData child = cast();
        GameData copy = child.simulationCopy();
        assertThat(copy.session).isNotSameAs(gd.session);
        assertThat(copy.session.root()).isNotSameAs(gd);
        copy.playerHands.get(player1.getId()).clear();
        copy.session.root().playerLifeTotals.put(player2.getId(), 1);
        assertThat(child.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(copy.session.root().pendingEffectResolutionEntry).isNotSameAs(gd.pendingEffectResolutionEntry);
    }

    @Test
    void headlessSimulationStartsAndKeepsTheChildWithoutMutatingTheLiveGame() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        harness.setHand(player1, List.of(new Shahrazad()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GameData copy = gd.simulationCopy();
        var simulator = com.github.laxika.magicalvibes.ai.simulation.HeadlessSimulationContext.getSimulator();
        simulator.applyAction(copy, player1.getId(),
                new com.github.laxika.magicalvibes.ai.simulation.SimulationAction.PlayCard(0, null, 0));
        simulator.applyAction(copy, player1.getId(),
                new com.github.laxika.magicalvibes.ai.simulation.SimulationAction.PassPriority());
        assertThat(copy.session.depth()).isEqualTo(1);
        assertThat(copy.session.active().status).isEqualTo(GameStatus.RUNNING);
        assertThat(simulator.isTerminal(copy)).isFalse();
        assertThat(gd.session.depth()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(30);
    }

    @Test
    void simulatedReturnDoesNotChangeOrUnregisterTheLiveSession() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        GameData child = cast();
        GameData copy = child.simulationCopy();
        gs.surrender(copy, player2);
        assertThat(copy.session.active()).isSameAs(copy.session.root());
        assertThat(copy.session.root().getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.session.active()).isSameAs(child);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void burningWishCanRetrieveTheResolvingShahrazad() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        GameData child = cast();
        Card source = gd.pendingEffectResolutionEntry.getCard();
        keep(child);
        castIn(child, new BurningWish(), ManaColor.RED, 2);
        PendingInteraction.LibrarySearch search = child.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int index = search.params().cards().indexOf(source);
        assertThat(index).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(child, player1, new InteractionAnswer.LibraryCardChosen(index));
        assertThat(child.playerHands.get(player1.getId())).contains(source);
        gs.surrender(child, player2);
        assertThat(gd.playerDecks.get(player1.getId())).contains(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(source);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void suspendedParentRejectsActions() {
        harness.setLibrary(player1, library(20));
        harness.setLibrary(player2, library(20));
        cast();
        assertThatThrownBy(() -> gs.passPriority(gd, player1)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.session.depth()).isEqualTo(1);
    }

    @Test
    @CardUsed({com.github.laxika.magicalvibes.cards.l.LivingWish.class,
            com.github.laxika.magicalvibes.cards.t.Thragtusk.class})
    void wishingForAParentPermanentDefersItsLeavesTriggerUntilTheParentResumes() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        Card thragtusk = new com.github.laxika.magicalvibes.cards.t.Thragtusk();
        harness.addToBattlefield(player1, thragtusk);
        GameData child = cast();
        keep(child);
        castIn(child, new com.github.laxika.magicalvibes.cards.l.LivingWish(), ManaColor.GREEN, 2);
        PendingInteraction.LibrarySearch search = child.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(child, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(thragtusk)));
        assertThat(child.playerHands.get(player1.getId())).contains(thragtusk);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.suspendedZoneTriggers).hasSize(1);
        assertThat(child.playerBattlefields.get(player1.getId())).isEmpty();
        gs.surrender(child, player2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.suspendedZoneTriggers).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
    }

    @Test
    void onlyThePlayerWithAShortOpeningLibraryLosesTheSubgame() {
        harness.setLibrary(player1, library(4));
        harness.setLibrary(player2, library(20));
        GameData child = cast();
        keep(child);
        assertThat(gd.session.active()).isSameAs(gd);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void mulliganWithTooFewCardsDoesNotCrashOrEraseTheFailedDraw() {
        harness.setLibrary(player1, library(3));
        harness.setLibrary(player2, library(20));
        GameData child = cast();
        gs.mulligan(child, player1);
        assertThat(child.playerHands.get(player1.getId())).hasSize(3);
        assertThat(child.playersAttemptedDrawFromEmptyLibrary).contains(player1.getId());
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    void parentLifeRestrictionsApplyOnlyWhenShahrazadResumes() {
        harness.setLibrary(player1, library(20));
        harness.setLibrary(player2, library(20));
        harness.addToBattlefield(player2, new PlatinumEmperion());
        GameData child = cast();
        assertThat(child.playerBattlefields.get(player2.getId())).isEmpty();
        gs.surrender(child, player2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void aDrawCanEndTheParentInASimultaneousDraw() {
        harness.setLibrary(player1, library(3));
        harness.setLibrary(player2, library(3));
        harness.setLife(player1, 1);
        harness.setLife(player2, 1);
        keep(cast());
        assertThat(gd.gameResult).isEqualTo(com.github.laxika.magicalvibes.model.event.GameEventFact.GameResult.DRAW);
    }

    @Test
    @CardUsed(Tazeem.class)
    void planechaseDeckReturnsWhileParentPlaneStaysInPlace() {
        harness.setLibrary(player1, library(20));
        harness.setLibrary(player2, library(20));
        gd.planechase = new com.github.laxika.magicalvibes.model.planar.PlanechaseState();
        var original = new com.github.laxika.magicalvibes.model.planar.PlanarObject(new Tazeem(), 1);
        gd.planechase.faceUp.add(original);
        gd.planechase.deck.add(new Tazeem());
        gd.planechase.deck.add(new Tazeem());
        GameData child = cast();
        assertThat(gd.planechase.faceUp).containsExactly(original);
        assertThat(gd.planechase.deck).isEmpty();
        keep(child);
        assertThat(child.planechase.faceUp).hasSize(1);
        gs.surrender(child, player2);
        assertThat(gd.planechase.faceUp).containsExactly(original);
        assertThat(gd.planechase.deck).hasSize(2);
    }

    @Test
    void counteredShahrazadDoesNotStartASubgame() {
        Shahrazad card = new Shahrazad();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);
        harness.castInstant(player2, 0, card.getId());
        harness.passBothPriorities();
        assertThat(gd.session.depth()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @CardUsed(Fork.class)
    void copiedShahrazadCreatesItsOwnSubgameBeforeTheOriginal() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        Shahrazad card = new Shahrazad();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorcery(player1, 0);
        harness.castInstant(player2, 0, card.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        assertThat(gd.session.depth()).isEqualTo(1);
        assertThat(gd.pendingEffectResolutionEntry.isCopy()).isTrue();
        gs.surrender(gd.session.active(), player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        harness.passBothPriorities();
        assertThat(gd.session.depth()).isEqualTo(1);
        assertThat(gd.pendingEffectResolutionEntry.isCopy()).isFalse();
        gs.surrender(gd.session.active(), player2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @CardUsed(KarnLiberated.class)
    void restartingAChildPreservesItsParentAndReturnContinuation() {
        harness.setLibrary(player1, library(30));
        harness.setLibrary(player2, library(30));
        GameData child = cast();
        keep(child);
        Permanent karn = new Permanent(new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 14);
        karn.setSummoningSick(false);
        child.playerBattlefields.get(player1.getId()).add(karn);
        child.activePlayerId = player1.getId();
        child.currentStep = TurnStep.PRECOMBAT_MAIN;
        child.priorityPassedBy.clear();
        gs.activateAbility(child, player1, 0, 2, null, null, null);
        for (int i = 0; i < 2 && !child.stack.isEmpty(); i++) {
            UUID priority = gqs.getPriorityPlayerId(child);
            gs.passPriority(child, priority.equals(player1.getId()) ? player1 : player2);
        }
        assertThat(child.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(gd.session.active()).isSameAs(child);
        assertThat(gd.waitingForSubgame).isTrue();
        gs.surrender(child, player2);
        assertThat(gd.session.active()).isSameAs(gd);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void returnsPhysicalCardsFromEveryChildZoneAndIgnoresTokens() {
        List<Card> cards = library(30);
        harness.setLibrary(player1, cards);
        harness.setLibrary(player2, library(30));
        GameData child = cast();
        var hand = child.playerHands.get(player1.getId());
        child.playerGraveyards.get(player1.getId()).add(hand.removeFirst());
        child.addToExile(player1.getId(), hand.removeFirst());
        Permanent stolen = new Permanent(hand.removeFirst());
        child.phasedOutPermanents.put(player2.getId(), new ArrayList<>(List.of(stolen)));
        child.stolenCreatures.put(stolen.getId(), player1.getId());
        Plains token = new Plains();
        token.setToken(true);
        child.playerBattlefields.get(player1.getId()).add(new Permanent(token));
        gs.surrender(child, player2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void concessionReturnsCardsTemporarilyHeldByAnUnansweredChoice() {
        List<Card> cards = library(20);
        cards.forEach(card -> card.setOwnerId(player1.getId()));
        harness.setLibrary(player1, cards);
        harness.setLibrary(player2, library(20));
        GameData child = cast();
        Card revealed = child.playerDecks.get(player1.getId()).removeFirst();
        child.interaction.beginInteraction(new PendingInteraction.LibraryRevealChoice(
                player1.getId(), List.of(revealed), List.of(revealed.getId()), false, false,
                false, true, false, 1, null, 0, "Choose a revealed card."));
        gs.surrender(child, player2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
    }

    @Test
    @CardUsed({com.github.laxika.magicalvibes.cards.g.GrafRats.class,
            com.github.laxika.magicalvibes.cards.m.MidnightScavengers.class})
    void bothPhysicalComponentsOfAMeldedPermanentReturn() {
        harness.setLibrary(player1, library(20));
        harness.setLibrary(player2, library(20));
        GameData child = cast();
        Card rats = new com.github.laxika.magicalvibes.cards.g.GrafRats();
        Card scavengers = new com.github.laxika.magicalvibes.cards.m.MidnightScavengers();
        Permanent melded = new Permanent(scavengers);
        melded.getMeldComponentCards().addAll(List.of(rats, scavengers));
        child.playerBattlefields.get(player1.getId()).add(melded);
        gs.surrender(child, player2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(22).contains(rats, scavengers);
    }
}
