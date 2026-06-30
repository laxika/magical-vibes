package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealXDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealXDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean tracksExcess = entry.getEffectsToResolve().stream()
                .anyMatch(ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect.class::isInstance);

        int damage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue(), entry);
        if (tracksExcess) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null || damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                entry.setExcessDamageDealt(0);
            } else {
                int markedBefore = target.getMarkedDamage();
                boolean deathtouch = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);
                entry.setExcessDamageDealt(damageSupport.computeExcessDamageToCreature(
                        gameData, target, damage, markedBefore, deathtouch));
            }
        }

        damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
    }
}
