package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "AKH", collectorNumber = "61")
@CardRegistration(set = "AKR", collectorNumber = "68")
public class LayClaim extends Card {

    public LayClaim() {
        // Enchant permanent — You control enchanted permanent.
        target(TargetFilters.permanent()).addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
