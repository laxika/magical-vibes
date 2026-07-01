package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class PermanentCopierService {

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride, Integer toughnessOverride) {
        applyCloneCopy(clonePerm, targetPerm, powerOverride, toughnessOverride, Set.of());
    }

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride,
                                Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        Card target = targetPerm.getCard();
        Card copy = new Card();
        copy.setName(target.getName());
        copy.setType(target.getType());
        copy.setAdditionalTypes(target.getAdditionalTypes());
        copy.setManaCost(target.getManaCost());
        copy.setColor(target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(powerOverride != null ? powerOverride : target.getPower());
        copy.setToughness(toughnessOverride != null ? toughnessOverride : target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        boolean hasPTOverride = powerOverride != null || toughnessOverride != null;
        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration reg : target.getEffectRegistrations(slot)) {
                // CR 707.9d: when a copy effect provides specific P/T values,
                // characteristic-defining abilities that define P/T are not copied
                if (hasPTOverride && reg.effect().isPowerToughnessDefining()) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }

        if (additionalTypesOverride != null && !additionalTypesOverride.isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(copy.getAdditionalTypes());
            for (CardType overrideType : additionalTypesOverride) {
                if (overrideType != copy.getType() && !merged.contains(overrideType)) {
                    merged.add(overrideType);
                }
            }
            copy.setAdditionalTypes(merged);
        }

        clonePerm.setCard(copy);
    }
}
