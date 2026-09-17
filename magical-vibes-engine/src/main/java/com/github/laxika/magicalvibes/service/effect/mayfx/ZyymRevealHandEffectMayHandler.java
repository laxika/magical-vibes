package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ZyymRevealHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Handles Zyym's repeated reveal choice and the final discard. */
@Component
@RequiredArgsConstructor
public class ZyymRevealHandEffectMayHandler implements MayEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ZyymRevealHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ZyymRevealHandEffect effect = ability.effects().stream()
                .filter(ZyymRevealHandEffect.class::isInstance)
                .map(ZyymRevealHandEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && effect.nextCardIndex() < effect.orderedCardIds().size()) {
            Card card = findCard(gameData, effect.targetPlayerId(),
                    effect.orderedCardIds().get(effect.nextCardIndex()));
            if (card != null) {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(effect.targetPlayerId()) + " reveals ", card, "."));
                cardRevealService.revealToAllPlayers(
                        gameData, effect.targetPlayerId(), GameEventFact.RevealZone.HAND, List.of(card));

                List<java.util.UUID> revealed = new ArrayList<>(effect.revealedCardIds());
                revealed.add(card.getId());
                int nextIndex = effect.nextCardIndex() + 1;
                if (nextIndex < effect.orderedCardIds().size()) {
                    enqueueNextReveal(gameData, ability, new ZyymRevealHandEffect(
                            effect.targetPlayerId(), effect.orderedCardIds(), revealed, nextIndex));
                } else {
                    discardMostRecentlyRevealed(gameData, effect.targetPlayerId(), revealed.getLast(),
                            ability.sourceCard(), ability.controllerId());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }
        }

        if (!effect.revealedCardIds().isEmpty()) {
            discardMostRecentlyRevealed(gameData, effect.targetPlayerId(),
                    effect.revealedCardIds().getLast(), ability.sourceCard(), ability.controllerId());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void enqueueNextReveal(GameData gameData, PendingMayAbility ability,
                                   ZyymRevealHandEffect nextEffect) {
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                ability.sourceCard(), ability.controllerId(), List.of(nextEffect),
                "Reveal another card?", nextEffect.targetPlayerId(), null,
                ability.sourcePermanentId()));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private Card findCard(GameData gameData, java.util.UUID playerId, java.util.UUID cardId) {
        return gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void discardMostRecentlyRevealed(GameData gameData, java.util.UUID playerId,
                                             java.util.UUID cardId, Card sourceCard,
                                             java.util.UUID sourceControllerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return;
        }
        Card card = findCard(gameData, playerId, cardId);
        if (card == null) {
            return;
        }

        gameData.discardCausedByOpponent = sourceControllerId != null
                && !sourceControllerId.equals(playerId);
        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, playerId)) {
            return;
        }

        hand.remove(card);
        triggerCollectionService.beginDiscardEvent(gameData, playerId);

        boolean replacedByBattlefield = gameData.discardCausedByOpponent
                && card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(EnterBattlefieldOnDiscardEffect.class::isInstance);
        if (replacedByBattlefield) {
            battlefieldEntryService.putPermanentOntoBattlefieldFromOpponentDiscard(
                    gameData, playerId, new Permanent(card));
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " discards ", card,
                    " — it enters the battlefield instead."));
        } else {
            graveyardService.discardCard(gameData, playerId, card);
            gameLogService.append(gameData, GameLog.playerDiscards(
                    gameData.playerIdToName.get(playerId), card));
        }

        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        triggerCollectionService.finishDiscardEvent(gameData);
        if (replacedByBattlefield && card.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, playerId, card, null, false);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }
}
