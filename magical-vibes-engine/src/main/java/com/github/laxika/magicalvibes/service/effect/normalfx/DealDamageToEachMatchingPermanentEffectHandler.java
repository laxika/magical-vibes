package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachMatchingPermanentEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null && damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;
        int evaluated = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);
        String cardName = source == null ? entry.getCard().getName() : source.getCard().getName();

        List<Permanent> candidates = new ArrayList<>();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield != null) candidates.addAll(battlefield);
        } else {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                candidates.addAll(battlefield);
            }
        }

        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(source == null ? entry.getCard().getId() : source.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(source)
                .withSourcePermanentId(entry.getSourcePermanentId());
        for (Permanent creature : new ArrayList<>(candidates)) {
            if (!predicateEvaluationService.matchesPermanentPredicate(creature, e.predicate(), ctx)) continue;
            if (!gameQueryService.isCreature(gameData, creature)) continue;
            if (gameQueryService.isDamagePreventable(gameData) && (source != null
                    ? gameQueryService.hasProtectionFromDamageSource(gameData, creature, source)
                    : gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard(), entry.getControllerId()))) {
                gameLogService.append(gameData, GameLog.textCardText(cardName + "'s damage to ", creature.getCard(), " is prevented."));
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage, source);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
