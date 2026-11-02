package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "55")
@CardRegistration(set = "WTH", collectorNumber = "30")
public class Abduction extends Card {

    public Abduction() {
        // Enchant creature
        target(TargetFilters.creature())
        // When this Aura enters, untap enchanted creature.
        .addTrigger(Triggers.enters(ObjectRelation.THIS), new UntapPermanentsEffect(TapUntapScope.ENCHANTED));

        // You control enchanted creature.
        addStaticEffect(new ControlEnchantedCreatureEffect());

        // When enchanted creature dies, return that card to the battlefield under its owner's control.
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect());
    }
}
