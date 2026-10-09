package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenPermanentDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChosenPermanentDealsPowerDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChosenPermanentDealsPowerDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damage = (ChosenPermanentDealsPowerDamageToTargetCreatureEffect) effect;
        if (damage.sourceFilter() != null && entry.getChosenPermanentId() == null) {
            var context = FilterContext.of(gameData).withSourceControllerId(entry.getControllerId());
            var eligible = gameData.playerBattlefields.get(entry.getControllerId()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            permanent, damage.sourceFilter(), context))
                    .map(Permanent::getId).toList();
            if (!eligible.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligible,
                        new PermanentChoiceContext.ChosenPermanentReference(),
                        "Choose a creature to deal damage.");
            }
            return;
        }
        Permanent source = entry.getChosenPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        Permanent target = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.hasProtectionFromSource(gameData, target, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, source);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, source);
    }
}
