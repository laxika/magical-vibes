package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetSourceBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a perpetual base-power and base-toughness change to the source card. */
@Component
@RequiredArgsConstructor
public class PerpetuallySetSourceBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetSourceBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallySetSourceBasePowerToughnessEffect set =
                (PerpetuallySetSourceBasePowerToughnessEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Card copy = source.getCard().createRuntimeCopy();
        copy.setPower(set.power());
        copy.setToughness(set.toughness());
        copy.freeze();
        source.exchangeCard(copy);
    }
}
