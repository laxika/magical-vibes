package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "FIC", collectorNumber = "336")
@CardRegistration(set = "C16", collectorNumber = "51")
public class ArmoryAutomaton extends Card {

    public ArmoryAutomaton() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"), 0, Integer.MAX_VALUE)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new AttachTargetEquipmentToTriggeringPermanentEffect(), "Attach the chosen Equipment?"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new MayEffect(new AttachTargetEquipmentToTriggeringPermanentEffect(), "Attach the chosen Equipment?"));
    }
}
