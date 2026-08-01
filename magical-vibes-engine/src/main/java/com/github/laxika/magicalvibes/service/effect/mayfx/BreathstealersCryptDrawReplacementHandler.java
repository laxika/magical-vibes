package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BreathstealersCryptDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Breathstealer's Crypt: after a real draw, reveal the card; if it's a creature, the drawing player
 * discards it unless they pay the effect's life cost. Auto-discards when payment is impossible.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BreathstealersCryptDrawReplacementHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BreathstealersCryptDrawReplacementEffect.class;
    }

    /**
     * Called from {@code DrawService.performDrawCard} after the card has entered hand. Reveals every
     * drawn card while a Crypt is on the battlefield; creature draws prompt pay-or-discard.
     */
    public void afterDraw(GameData gameData, UUID drawingPlayerId, Card drawn) {
        CryptSource crypt = findCryptSource(gameData);
        if (crypt == null) {
            return;
        }

        String drawerName = gameData.playerIdToName.get(drawingPlayerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(drawerName + " reveals ")
                .card(drawn)
                .text(" with ")
                .card(crypt.sourceCard())
                .text(".")
                .build());

        if (!drawn.hasType(CardType.CREATURE)) {
            return;
        }

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, drawingPlayerId)
                && gameData.getLife(drawingPlayerId) >= crypt.effect().lifeCost();
        if (!canPay) {
            discardDrawnCard(gameData, drawingPlayerId, drawn.getId(), crypt.sourceCard(), crypt.controllerId());
            return;
        }

        String prompt = "Pay " + crypt.effect().lifeCost() + " life? If you don't, discard "
                + drawn.getName() + ". (" + crypt.sourceCard().getName() + ")";
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                crypt.sourceCard(),
                drawingPlayerId,
                List.of(crypt.effect()),
                prompt,
                drawn.getId(),
                null,
                crypt.permanentId()));
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        BreathstealersCryptDrawReplacementEffect effect = ability.effects().stream()
                .filter(BreathstealersCryptDrawReplacementEffect.class::isInstance)
                .map(BreathstealersCryptDrawReplacementEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID playerId = ability.controllerId();
        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= effect.lifeCost();
        boolean paid = accepted && canPay;

        if (paid) {
            gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - effect.lifeCost());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + effect.lifeCost() + " life. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} pays {} life to keep drawn creature ({})",
                    gameData.id, player.getUsername(), effect.lifeCost(), ability.sourceCard().getName());
        } else {
            UUID cryptControllerId = ability.sourcePermanentId() != null
                    ? gameQueryService.findPermanentController(gameData, ability.sourcePermanentId())
                    : null;
            discardDrawnCard(gameData, playerId, ability.targetCardId(), ability.sourceCard(), cryptControllerId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void discardDrawnCard(GameData gameData, UUID playerId, UUID cardId, Card sourceCard,
                                  UUID cryptControllerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return;
        }
        Card card = hand.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
        if (card == null) {
            return;
        }

        hand.remove(card);
        gameData.discardCausedByOpponent = cryptControllerId != null && !cryptControllerId.equals(playerId);
        graveyardService.discardCard(gameData, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " discards ", card, "."));
        log.info("Game {} - {} discards {} ({})", gameData.id,
                gameData.playerIdToName.get(playerId), card.getName(), sourceCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }

    private CryptSource findCryptSource(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof BreathstealersCryptDrawReplacementEffect crypt) {
                        return new CryptSource(permanent.getCard(), permanent.getId(), pid, crypt);
                    }
                }
            }
        }
        return null;
    }

    private record CryptSource(Card sourceCard, UUID permanentId, UUID controllerId,
                               BreathstealersCryptDrawReplacementEffect effect) {
    }
}
