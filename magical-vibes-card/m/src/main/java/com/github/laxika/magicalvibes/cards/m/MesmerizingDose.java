package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "62")
public class MesmerizingDose extends Card {

    public MesmerizingDose() {
        target(TargetFilters.creature())
                // When this Aura enters, tap enchanted creature, then proliferate.
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(TapUntapScope.ENCHANTED),
                        new ProliferateEffect())
                // Enchanted creature doesn't untap during its controller's untap step.
                .addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
