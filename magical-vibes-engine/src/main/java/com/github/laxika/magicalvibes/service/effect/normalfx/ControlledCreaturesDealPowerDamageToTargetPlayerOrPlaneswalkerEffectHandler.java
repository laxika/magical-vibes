package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent targetPermanent = null;
        if (!gameData.playerIds.contains(targetId)) {
            targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null || !targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
                return;
            }
        }

        List<Permanent> sources = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            FilterContext context = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), context)) {
                    sources.add(permanent);
                }
            }
        }

        for (Permanent source : sources) {
            if (gameQueryService.findPermanentById(gameData, source.getId()) == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (controllerId == null) {
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
                continue;
            }

            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    List.of(),
                    null,
                    source.getId());
            int power = gameQueryService.getPowerBasedDamage(gameData, source);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
            if (targetPermanent != null) {
                damageSupport.resolveAnyTargetDamage(gameData, damageEntry, targetId, rawDamage, false);
            } else {
                damageSupport.dealDamageToPlayer(gameData, damageEntry, targetId, rawDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
