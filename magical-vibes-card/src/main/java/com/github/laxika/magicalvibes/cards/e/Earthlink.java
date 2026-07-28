package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "285")
public class Earthlink extends Card {

    public Earthlink() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {2}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{2}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever a creature dies, that creature's controller sacrifices a land of their choice.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new DyingCreatureControllerSacrificesPermanentsEffect(1, new PermanentIsLandPredicate()));
    }
}
