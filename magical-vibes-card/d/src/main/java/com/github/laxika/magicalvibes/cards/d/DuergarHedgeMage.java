package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EVE", collectorNumber = "137")
@CardRegistration(set = "HOP", collectorNumber = "101")
@CardRegistration(set = "CMD", collectorNumber = "195")
@CardRegistration(set = "MOC", collectorNumber = "324")
@CardRegistration(set = "CMA", collectorNumber = "200")
public class DuergarHedgeMage extends Card {

    public DuergarHedgeMage() {
        target(TargetFilters.artifact(), 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target artifact?")),
                        TriggerMode.INDEPENDENT);

        target(TargetFilters.enchantment(), 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target enchantment?")),
                        TriggerMode.INDEPENDENT);
    }
}
