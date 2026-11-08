package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDrawsCardsEqualToAttachedCountEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachTargetPlayerDrawsCardsEqualToAttachedCountEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerDrawsCardsEqualToAttachedCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var drawEffect = (EachTargetPlayerDrawsCardsEqualToAttachedCountEffect) effect;
        // "Attached to that player": the triggering player, counted as the ability resolves.
        int amount = entry.getTriggeringPlayerId() == null ? 0
                : damageSupport.countPermanentsAttachedToPlayer(gameData, entry.getTriggeringPlayerId(), drawEffect.filter());
        for (var playerId : entry.getTargetIds()) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }
        }
    }
}
