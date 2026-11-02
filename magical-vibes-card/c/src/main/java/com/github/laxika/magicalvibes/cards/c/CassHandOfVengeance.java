package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "97")
@CardRegistration(set = "PIP", collectorNumber = "410")
@CardRegistration(set = "PIP", collectorNumber = "625")
@CardRegistration(set = "PIP", collectorNumber = "938")
public class CassHandOfVengeance extends Card {

    public CassHandOfVengeance() {
        // "If it was enchanted or equipped" is about whichever creature died, Cass included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        var enchantedOrEquipped = new PermanentAnyOfPredicate(List.of(
                new PermanentIsEnchantedPredicate(),
                new PermanentIsEquippedPredicate()));
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(enchantedOrEquipped, ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                enchantedOrEquipped, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect()))));
    }
}
