package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FreeEquipWhileEnduringStoryEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "17")
public class KLiTheResourceful extends Card {

    public KLiTheResourceful() {
        addStaticEffect(new StoriedEffect());
        addStaticEffect(new FreeEquipWhileEnduringStoryEffect());
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DWARF),
                        new OncePerTurnTriggerEffect(new DrawCardEffect())));
        addTrigger(Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
