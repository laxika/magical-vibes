package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "222")
@CardRegistration(set = "M15", collectorNumber = "235")
public class StaffOfTheSunMagus extends Card {

    public StaffOfTheSunMagus() {
        // Whenever you cast a white spell or a Plains you control enters, you gain 1 life.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.WHITE),
                List.of(new GainLifeEffect(1))));

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                        new GainLifeEffect(1)));
    }
}
