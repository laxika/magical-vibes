package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileUnlessDiscardCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUnlessDiscardCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileUnlessDiscardCardTypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean hasValidCard = hand != null && hand.stream()
                .anyMatch(card -> e.requiredType() == null || card.getType() == e.requiredType());
        String typeName = e.requiredType() == null
                ? "card"
                : e.requiredType().name().toLowerCase() + " card";

        if (!hasValidCard) {
            if (sourcePermanent != null) {
                permanentRemovalService.removePermanentToExile(gameData, sourcePermanent);
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " has no " + typeName + " to discard. ")
                        .card(sourceCard)
                        .text(" is exiled.")
                        .build());
                log.info("Game {} - {} exiled (no {} to discard)", gameData.id, sourceCard.getName(), typeName);
            }
            return;
        }

        String prompt = sourcePermanent != null
                ? "Discard a " + typeName + "? If you don't, " + sourceCard.getName() + " will be exiled."
                : sourceCard.getName() + " is no longer on the battlefield. Discard a " + typeName + " anyway?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(e), prompt, null, null, entry.getSourcePermanentId()));
    }
}
