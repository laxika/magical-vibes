package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Braids's per-opponent sacrifice-or-life-loss-and-draw clause in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DrawCardEffectHandler drawCardEffectHandler;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var braidsEffect = (EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect) effect;
        for (UUID opponentId : AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                .apnapOpponents(gameData, entry.getControllerId())) {
            if (hasLegalSacrifice(gameData, entry, opponentId)) {
                gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                        entry.getCard(),
                        opponentId,
                        List.of(effect),
                        "Sacrifice a permanent sharing a card type with it?",
                        null,
                        null,
                        entry.getSourcePermanentId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        null,
                        entry.getSacrificedPermanentSnapshot(),
                        entry.getControllerId(),
                        null,
                        0));
            } else {
                applyFallback(gameData, entry, opponentId, braidsEffect.lifeLoss());
            }
        }
    }

    boolean hasLegalSacrifice(GameData gameData, StackEntry entry, UUID playerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())
                || entry.getSacrificedPermanentSnapshot() == null) {
            return false;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSacrificedPermanentSnapshot());
        var filter = new PermanentSharesCardTypeWithSourcePermanentPredicate();
        return !destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context))
                .isEmpty();
    }

    private void applyFallback(GameData gameData, StackEntry entry, UUID opponentId, int lifeLoss) {
        lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, entry.getCard().getName());
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
    }
}
