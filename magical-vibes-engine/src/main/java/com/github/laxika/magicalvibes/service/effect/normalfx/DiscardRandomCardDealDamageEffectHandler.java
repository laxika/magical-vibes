package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscardRandomCardDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DiscardRandomCardDealDamageEffect damageEffect = (DiscardRandomCardDealDamageEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null || entry.getDiscardedCardSnapshot() == null) {
            return;
        }

        int damage = entry.getDiscardedCardSnapshot().getColors().size() >= 2
                ? damageEffect.multicoloredDamage()
                : damageEffect.damage();
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
