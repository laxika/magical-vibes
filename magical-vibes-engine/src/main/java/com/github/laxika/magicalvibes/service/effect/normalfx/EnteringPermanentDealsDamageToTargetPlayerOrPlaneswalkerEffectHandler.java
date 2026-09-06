package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToTargetPlayerOrPlaneswalkerEffectHandler delegate;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source != null) {
            entry.setDamageSourceCard(source.getCard());
        }

        var e = (EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect) effect;
        delegate.resolve(gameData, entry, new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                e.amount(), e.playerRelation()));
    }
}
