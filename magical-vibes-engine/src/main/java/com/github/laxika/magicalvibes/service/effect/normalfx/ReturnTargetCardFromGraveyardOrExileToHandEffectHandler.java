package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardOrExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardFromGraveyardOrExileToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardFromGraveyardOrExileToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCardFromGraveyardOrExileToHandEffect) effect;
        UUID targetId = entry.getTargetId();
        Zone targetZone = entry.getTargetZone();
        if (targetId == null || (targetZone != Zone.GRAVEYARD && targetZone != Zone.EXILE)) {
            fizzle(gameData, entry, "no valid graveyard or exile target");
            return;
        }

        Card targetCard;
        Card predicateCard;
        if (targetZone == Zone.GRAVEYARD) {
            targetCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
            UUID graveyardOwner = gameQueryService.findGraveyardOwnerById(gameData, targetId);
            if (targetCard == null || !entry.getControllerId().equals(graveyardOwner)) {
                fizzle(gameData, entry, "target is no longer in your graveyard");
                return;
            }
            predicateCard = targetCard;
            if (!predicateEvaluationService.matchesCardPredicate(predicateCard, e.graveyardFilter(), null)) {
                fizzle(gameData, entry, "target is not a " + CardPredicateUtils.describeFilter(e.graveyardFilter()));
                return;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, targetId);
        } else {
            targetCard = gameQueryService.findCardInExileById(gameData, targetId);
            UUID exileOwner = gameQueryService.findExileOwnerById(gameData, targetId);
            if (targetCard == null || e.exileOwnedOnly() && !entry.getControllerId().equals(exileOwner)) {
                fizzle(gameData, entry, "target is no longer in your exile zone");
                return;
            }
            predicateCard = targetCard;
            if (!predicateEvaluationService.matchesCardPredicate(predicateCard, e.exileFilter(), null)) {
                fizzle(gameData, entry, "target is not a " + CardPredicateUtils.describeFilter(e.exileFilter()));
                return;
            }
            gameData.removeFromExile(targetId);
        }

        gameData.addCardToHand(entry.getControllerId(), targetCard);
        String zoneName = targetZone == Zone.GRAVEYARD ? "graveyard" : "exile";
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getDescription() + " returns ", targetCard, " from " + zoneName + " to hand."));
    }

    private void fizzle(GameData gameData, StackEntry entry, String reason) {
        gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (" + reason + ")."));
    }
}
