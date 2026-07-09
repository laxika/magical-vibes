package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.UUID;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MassDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MassDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MassDamageEffect) effect;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        // Source-relative amounts use the live source permanent when present, else the
        // last-known snapshot (matching LoseLifeEffectHandler).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int baseDamage = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage, entry);

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        Predicate<Permanent> baseFilter = e.damagesPlaneswalkers()
                ? p -> gameQueryService.isCreature(gameData, p) || p.getCard().hasType(CardType.PLANESWALKER)
                : p -> gameQueryService.isCreature(gameData, p);
        Predicate<Permanent> creatureFilter = e.filter() == null
                ? baseFilter
                : p -> baseFilter.test(p)
                        && predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext);

        damageSupport.damageAllCreaturesOnBattlefield(gameData, entry, damage, creatureFilter);

        if (e.damagesPlayers()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
            }
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
