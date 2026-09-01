package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import org.springframework.stereotype.Component;

@Component
public class ExileSpellAndReturnTransformedEffectHandler implements NormalEffectHandlerBean {

    private final ExileAndReturnTransformedService exileAndReturnTransformedService;

    public ExileSpellAndReturnTransformedEffectHandler(
            ExileAndReturnTransformedService exileAndReturnTransformedService) {
        this.exileAndReturnTransformedService = exileAndReturnTransformedService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSpellAndReturnTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourceZone() != Zone.GRAVEYARD) {
            return;
        }

        if (exileAndReturnTransformedService.exileSpellAndReturnTransformed(gameData, entry)) {
            entry.setSpellDispositionHandled(true);
        }
    }
}
