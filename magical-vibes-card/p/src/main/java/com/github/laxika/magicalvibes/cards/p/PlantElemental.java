package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "POR", collectorNumber = "181")
public class PlantElemental extends Card {

    public PlantElemental() {
        // When Plant Elemental enters, sacrifice it unless you sacrifice a Forest.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
