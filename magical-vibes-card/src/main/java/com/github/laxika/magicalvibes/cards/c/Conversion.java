package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "19")
public class Conversion extends Card {

    public Conversion() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // All Mountains are Plains.
        addEffect(EffectSlot.STATIC,
                new LandsOfSubtypeBecomeTypeEffect(CardSubtype.MOUNTAIN, CardSubtype.PLAINS));
    }
}
