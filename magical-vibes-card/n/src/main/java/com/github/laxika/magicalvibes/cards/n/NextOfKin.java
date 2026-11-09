package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "62")
@CardRegistration(set = "NCC", collectorNumber = "162")
public class NextOfKin extends Card {

    public NextOfKin() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.creatureDies(ObjectRelation.ENCHANTED), new MayEffect(
                        new PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect(),
                        "Put a creature card with lesser mana value from your hand or the command zone onto the"
                                + " battlefield?"));
    }
}
