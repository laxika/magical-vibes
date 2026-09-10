package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldOrElseEffect;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PutCardToBattlefieldOrElseEffectHandler implements NormalEffectHandlerBean {

    private final EffectResolutionService effectResolutionService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    public PutCardToBattlefieldOrElseEffectHandler(
            @Lazy EffectResolutionService effectResolutionService,
            PlayerInteractionSupport playerInteractionSupport,
            PredicateEvaluationService predicateEvaluationService) {
        this.effectResolutionService = effectResolutionService;
        this.playerInteractionSupport = playerInteractionSupport;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldOrElseEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        boolean hasMatchingCard = hand != null && hand.stream().anyMatch(card ->
                predicateEvaluationService.matchesCardPredicate(card, e.predicate(), sourceCardId,
                        gameData, entry.getControllerId()));

        if (!hasMatchingCard) {
            resolveFallback(gameData, entry, e.elseEffect());
            return;
        }

        playerInteractionSupport.applyPutCardToBattlefield(gameData, entry.getControllerId(),
                new PutCardToBattlefieldEffect(e.predicate(), e.label()), entry.getXValue(), null, sourceCardId);
    }

    private void resolveFallback(GameData gameData, StackEntry entry, CardEffect fallback) {
        if (fallback == null) {
            return;
        }
        StackEntry fallbackEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(fallback), entry.getTargetId(), entry.getSourcePermanentId());
        fallbackEntry.setActivePlayerId(entry.getActivePlayerId());
        fallbackEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        fallbackEntry.setAttackedTargetId(entry.getAttackedTargetId());
        effectResolutionService.resolveEffects(gameData, fallbackEntry);
    }
}
