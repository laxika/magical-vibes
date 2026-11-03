package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "40")
public class WeaponsVendor extends Card {

    public WeaponsVendor() {
        AttachTargetEquipmentToTargetCreatureEffect attach = new AttachTargetEquipmentToTargetCreatureEffect();
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control")).bindEffect(attach);
        target(TargetFilters.creatureYouControl()).bindEffect(attach);

        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))),
                new MayPayManaEffect(
                        "{1}",
                        new QueueReflexiveAbilityEffect(attach),
                        "Pay {1} to attach an Equipment you control to a creature you control?"));
    }
}
