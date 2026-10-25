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
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Braids's per-opponent sacrifice-or-life-loss-and-draw clause in APNAP order: an opponent with a matching
 * permanent to sacrifice is asked, one without loses the life at once and its controller draws.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: before anything changes, the Lich's Duel Mastery sacrifices the
 * at-once losses would cause are asked about (CR 616.1) through
 * {@link LifeSupport#askBeforeLifeLossesThatFollowAChange}, since a draw or an earlier opponent's prompt can come
 * before a later opponent's loss.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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
        List<UUID> opponents = AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                .apnapOpponents(gameData, entry.getControllerId());
        Map<UUID, Integer> lossesAtOnce = new LinkedHashMap<>();
        for (UUID opponentId : opponents) {
            if (!hasLegalSacrifice(gameData, entry, opponentId)) {
                lossesAtOnce.put(opponentId, braidsEffect.lifeLoss());
            }
        }
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, lossesAtOnce);

        for (UUID opponentId : opponents) {
            if (!lossesAtOnce.containsKey(opponentId)) {
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
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context))
                .isEmpty();
    }

    private void applyFallback(GameData gameData, StackEntry entry, UUID opponentId, int lifeLoss) {
        lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, entry.getCard().getName());
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
    }
}
