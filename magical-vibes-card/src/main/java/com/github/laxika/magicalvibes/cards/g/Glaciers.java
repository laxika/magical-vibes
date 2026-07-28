package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "294")
public class Glaciers extends Card {

    public Glaciers() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{U}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // All Mountains are Plains.
        addEffect(EffectSlot.STATIC,
                new LandsOfSubtypeBecomeTypeEffect(CardSubtype.MOUNTAIN, CardSubtype.PLAINS));
    }
}
