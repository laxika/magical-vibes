package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureOfEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoostEquippedCreatureOfEnchantedEquipmentEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEquippedCreatureOfEnchantedEquipmentEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent aura = context.source();
        if (!aura.isAttached()) return;

        Permanent equipment = findPermanentById(context.gameData(), aura.getAttachedTo());
        if (equipment == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || !equipment.isAttached()
                || !equipment.getAttachedTo().equals(context.target().getId())) {
            return;
        }

        var boost = (BoostEquippedCreatureOfEnchantedEquipmentEffect) effect;
        accumulator.addPower(boost.powerBoost());
        accumulator.addToughness(boost.toughnessBoost());
    }

    private Permanent findPermanentById(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        final Permanent[] found = {null};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanentId.equals(permanent.getId())) {
                found[0] = permanent;
            }
        });
        return found[0];
    }
}
