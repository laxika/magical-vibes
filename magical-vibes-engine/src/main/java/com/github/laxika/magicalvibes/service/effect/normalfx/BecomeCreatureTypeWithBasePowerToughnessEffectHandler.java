package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeCreatureTypeWithBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCreatureTypeWithBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BecomeCreatureTypeWithBasePowerToughnessEffect) effect;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        // Intervening "if": e.g. "If this creature is a Spirit, ...". Granted subtypes count.
        if (e.requiredSubtype() != null
                && !source.getCard().getSubtypes().contains(e.requiredSubtype())
                && !source.getGrantedSubtypes().contains(e.requiredSubtype())) {
            return;
        }

        // Set base P/T as a layer-7b setting effect (CR 613.4b); the timestamp orders it against
        // other 7b setters. A later level-up simply gets a newer timestamp and wins.
        source.setBasePowerOverriddenPermanently(true);
        source.setPermanentBasePowerOverride(e.power());
        source.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
        source.setBaseToughnessOverriddenPermanently(true);
        source.setPermanentBaseToughnessOverride(e.toughness());
        source.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());

        if (!source.getGrantedSubtypes().contains(e.addedSubtype())) {
            source.getGrantedSubtypes().add(e.addedSubtype());
        }

        gameBroadcastService.logAndBroadcast(gameData,
                source.getCard().getName() + " becomes a " + e.addedSubtype().getDisplayName()
                        + " with base power and toughness " + e.power() + "/" + e.toughness() + ".");
    }
}
