package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetAndSharingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetAndSharingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTargetAndSharingCreaturesEffect) effect;
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            setEventValueIfLifeGainReadsIt(entry, 0);
            return;
        }

        UUID targetId = entry.targetsForEffect(damageEffect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            setEventValueIfLifeGainReadsIt(entry, 0);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            setEventValueIfLifeGainReadsIt(entry, 0);
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluated = amountEvaluationService.evaluate(gameData, damageEffect.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);
        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);

        List<Permanent> affectedCreatures = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (permanent.getId().equals(targetId)
                    || (!targetColors.isEmpty()
                    && gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .anyMatch(targetColors::contains)))) {
                affectedCreatures.add(permanent);
            }
        });

        int damageDealt = 0;
        for (Permanent creature : affectedCreatures) {
            damageDealt += damageSupport.dealCreatureDamage(gameData, entry, creature, damage);
        }
        setEventValueIfLifeGainReadsIt(entry, damageDealt);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private void setEventValueIfLifeGainReadsIt(StackEntry entry, int damageDealt) {
        if (entry.getEffectsToResolve().stream().anyMatch(effect -> effect instanceof LifeGainEffect lifeGain
                && amountEvaluationService.referencesEventValue(lifeGain.lifeGainAmount()))) {
            entry.setEventValue(damageDealt);
        }
    }
}
