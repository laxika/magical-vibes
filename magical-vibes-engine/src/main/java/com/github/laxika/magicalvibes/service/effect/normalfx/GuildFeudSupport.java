package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingGuildFeud;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Drives Guild Feud's two sequential reveal stages and the closing fight. Each stage reveals a
 * player's top three cards and reuses the generic {@code LibraryRevealChoice} battlefield flow
 * (max one pick, remainder to the graveyard); {@link PendingGuildFeud} carries the stage and the
 * opponent's chosen permanent across the async boundary.
 */
@Component
@RequiredArgsConstructor
public class GuildFeudSupport {

    private static final int REVEAL_COUNT = 3;

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final FightSupport fightSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /** Starts the flow with the targeted opponent's reveal. */
    public void begin(GameData gameData, StackEntry entry, UUID opponentId) {
        run(gameData, new PendingGuildFeud(entry.getControllerId(), opponentId, false, null));
    }

    /**
     * Advances the flow after a Guild Feud reveal choice was answered. Returns {@code true} when a
     * further interaction is now pending, so the caller must not resume the parked stack entry yet.
     */
    public boolean onRevealAnswered(GameData gameData, List<Card> selectedCards) {
        PendingGuildFeud state = gameData.pollPendingInteraction(PendingGuildFeud.class);
        if (state == null) {
            return false;
        }
        UUID revealerId = state.controllerStage() ? state.controllerId() : state.opponentId();
        UUID chosenPermanentId = findPermanent(gameData, revealerId, selectedCards);

        if (!state.controllerStage()) {
            return run(gameData, new PendingGuildFeud(
                    state.controllerId(), state.opponentId(), true, chosenPermanentId));
        }
        finish(gameData, state.opponentCreaturePermanentId(), chosenPermanentId);
        return false;
    }

    /** Runs the flow from {@code state} onward; {@code true} means an interaction is now pending. */
    private boolean run(GameData gameData, PendingGuildFeud state) {
        if (!state.controllerStage()) {
            if (beginReveal(gameData, state.opponentId(), state)) {
                return true;
            }
            return run(gameData, new PendingGuildFeud(
                    state.controllerId(), state.opponentId(), true, null));
        }
        if (beginReveal(gameData, state.controllerId(), state)) {
            return true;
        }
        finish(gameData, state.opponentCreaturePermanentId(), null);
        return false;
    }

    /**
     * Reveals {@code revealerId}'s top three cards. Returns {@code true} when a choice prompt was
     * begun; {@code false} when the stage completed synchronously (no cards, or no creature card
     * among them — everything revealed goes straight to the graveyard).
     */
    private boolean beginReveal(GameData gameData, UUID revealerId, PendingGuildFeud state) {
        List<Card> deck = gameData.playerDecks.get(revealerId);
        String playerName = gameData.playerIdToName.get(revealerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
            return false;
        }

        List<Card> revealed = LibraryRevealSupport.takeTopCards(deck, Math.min(REVEAL_COUNT, deck.size()));
        logReveal(gameData, playerName, revealed);

        List<UUID> creatureIds = revealed.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .collect(Collectors.toList());

        if (creatureIds.isEmpty()) {
            revealed.forEach(card -> graveyardService.addCardToGraveyard(gameData, revealerId, card));
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals no creature card and puts the revealed cards into their graveyard."));
            return false;
        }

        gameData.queueInteraction(state);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                revealerId, revealed, creatureIds, true, false, false, false, false,
                0, null, 1,
                "You may put a creature card from among the revealed cards onto the battlefield. The rest go to your graveyard."));
        return true;
    }

    /** Has the two creatures put onto the battlefield this way fight, if there are two of them. */
    private void finish(GameData gameData, UUID opponentPermanentId, UUID controllerPermanentId) {
        if (opponentPermanentId == null || controllerPermanentId == null) {
            return;
        }
        Permanent first = gameQueryService.findPermanentById(gameData, opponentPermanentId);
        Permanent second = gameQueryService.findPermanentById(gameData, controllerPermanentId);
        if (first == null || second == null) {
            return;
        }
        gameLogService.append(gameData, GameLog.cardTextCard(first.getCard(), " fights ", second.getCard(), "."));
        fightSupport.fight(gameData, gameData.pendingEffectResolutionEntry, first, second);
    }

    private UUID findPermanent(GameData gameData, UUID controllerId, List<Card> selectedCards) {
        if (selectedCards == null || selectedCards.isEmpty()) {
            return null;
        }
        Set<UUID> cardIds = selectedCards.stream().map(Card::getId).collect(Collectors.toSet());
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> cardIds.contains(permanent.getCard().getId()))
                .map(Permanent::getId)
                .findFirst()
                .orElse(null);
    }

    private void logReveal(GameData gameData, String playerName, List<Card> revealed) {
        GameLog.Builder builder = GameLog.builder().text(playerName + " reveals ");
        for (int i = 0; i < revealed.size(); i++) {
            if (i > 0) {
                builder.text(i == revealed.size() - 1 ? " and " : ", ");
            }
            builder.card(revealed.get(i));
        }
        gameLogService.append(gameData, builder.text(" from the top of their library.").build());
    }
}
