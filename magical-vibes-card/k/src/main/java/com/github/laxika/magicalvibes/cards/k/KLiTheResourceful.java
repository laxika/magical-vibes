package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FreeEquipWhileEnduringStoryEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "17")
public class KLiTheResourceful extends Card {

    public KLiTheResourceful() {
        addStaticEffect(new StoriedEffect());
        addStaticEffect(new FreeEquipWhileEnduringStoryEffect());
        // "Whenever another Dwarf you control enters and whenever an Equipment you control enters, draw a card.
        // This ability triggers only once each turn": one ability of two trigger conditions under one limit.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                        new PermanentHasSubtypePredicate(CardSubtype.DWARF))), ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(new DrawCardEffect()))));
    }
}
