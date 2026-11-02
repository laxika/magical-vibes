package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "336")
@CardRegistration(set = "C16", collectorNumber = "51")
public class ArmoryAutomaton extends Card {

    public ArmoryAutomaton() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"), 0, Integer.MAX_VALUE)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(new AttachTargetEquipmentToTriggeringPermanentEffect(), "Attach the chosen Equipment?"))
                .addTrigger(Triggers.attacks(),
                        new MayEffect(new AttachTargetEquipmentToTriggeringPermanentEffect(), "Attach the chosen Equipment?"));
    }
}
