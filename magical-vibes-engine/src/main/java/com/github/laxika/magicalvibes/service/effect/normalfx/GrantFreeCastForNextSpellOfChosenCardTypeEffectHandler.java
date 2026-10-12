package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextSpellOfChosenCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantFreeCastForNextSpellOfChosenCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFreeCastForNextSpellOfChosenCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // CR 113.7a / 608.2h: if the source has left the battlefield, its last known information is used.
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getChosenCardType() == null) {
            return;
        }
        gameData.addNextSpellFreeCastPermission(entry.getControllerId(),
                new CardTypePredicate(source.getChosenCardType()));
    }
}
