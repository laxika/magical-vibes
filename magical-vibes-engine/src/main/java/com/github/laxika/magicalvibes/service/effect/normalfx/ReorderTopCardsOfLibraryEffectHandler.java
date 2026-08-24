package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryDecisionMaker;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReorderTopCardsOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReorderTopCardsOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReorderTopCardsOfLibraryEffect reorder = (ReorderTopCardsOfLibraryEffect) effect;

        UUID controllerId = entry.getControllerId();
        UUID decisionMakerId = resolveDecisionMaker(entry, reorder.decisionMaker(), controllerId);
        var source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, reorder.count(),
                AmountContext.forStackEntry(entry, source)));
        UUID deckOwnerId = resolveDeckOwner(entry, reorder.owner(), controllerId);
        List<Card> deck = gameData.playerDecks.get(deckOwnerId);
        String decisionMakerName = gameData.playerIdToName.get(decisionMakerId);
        boolean ownLibrary = deckOwnerId.equals(decisionMakerId);
        String libraryOf = ownLibrary ? "their library" : gameData.playerIdToName.get(deckOwnerId) + "'s library";

        count = Math.min(count, deck.size());
        if (count == 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ownLibrary
                    ? ": library is empty, nothing to reorder."
                    : ": " + gameData.playerIdToName.get(deckOwnerId) + "'s library is empty, nothing to reorder."));
            return;
        }

        if (count == 1) {
            gameLogService.append(gameData,
                    GameLog.text(decisionMakerName + " looks at the top card of " + libraryOf + "."));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                decisionMakerId, topCards, false, deckOwnerId,
                "Put these cards back on top of " + (ownLibrary ? "your" : libraryOf)
                        + " in any order (top to bottom)."));

        gameLogService.append(gameData, GameLog.text(
                decisionMakerName + " looks at the top " + count + " cards of " + libraryOf + "."));
        log.info("Game {} - {} reordering top {} cards of {}", gameData.id, decisionMakerName, count, libraryOf);
    }

    /** Falls back to the controller when a target-player form resolves without a target. */
    private static UUID resolveDeckOwner(StackEntry entry, LibraryOwner owner, UUID controllerId) {
        if (owner != LibraryOwner.TARGET_PLAYER) return controllerId;
        return entry.getTargetId() != null ? entry.getTargetId() : controllerId;
    }

    /** Falls back to the controller when a target-player decision resolves without a target. */
    private static UUID resolveDecisionMaker(StackEntry entry, LibraryDecisionMaker decisionMaker,
                                             UUID controllerId) {
        if (decisionMaker != LibraryDecisionMaker.TARGET_PLAYER) return controllerId;
        return entry.getTargetId() != null ? entry.getTargetId() : controllerId;
    }
}
