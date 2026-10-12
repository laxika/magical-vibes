package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetBasePowerToughnessFromSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetTargetBasePowerToughnessFromSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetBasePowerToughnessFromSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent live = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // A source that left the battlefield is read from its last-known information, which still
        // includes bonuses from Auras and other effects that stopped applying when it left.
        Permanent source = live != null ? live : entry.getSourcePermanentSnapshot();

        int power = source == null ? 0
                : live == null && source.getLastKnownPower() != null
                ? source.getLastKnownPower() : gameQueryService.getEffectivePower(gameData, source);
        int toughness = source == null ? 0
                : live == null && source.getLastKnownToughness() != null
                ? source.getLastKnownToughness() : gameQueryService.getEffectiveToughness(gameData, source);

        setBasePowerToughnessEffectHandler.resolve(gameData, entry,
                new SetBasePowerToughnessEffect(power, toughness));
    }
}
