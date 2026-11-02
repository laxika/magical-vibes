package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "96")
public class EnormousEnergyBlade extends Card {

    public EnormousEnergyBlade() {
        addStaticEffect(new StaticBoostEffect(4, 0, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.becomesAttachedToCreature(),
                new TapPermanentsEffect(TapUntapScope.ENCHANTED));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
