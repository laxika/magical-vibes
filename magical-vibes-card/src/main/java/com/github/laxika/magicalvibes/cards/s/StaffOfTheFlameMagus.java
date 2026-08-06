package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "220")
@CardRegistration(set = "M15", collectorNumber = "233")
public class StaffOfTheFlameMagus extends Card {

    public StaffOfTheFlameMagus() {
        // Whenever you cast a red spell or a Mountain you control enters, you gain 1 life.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new GainLifeEffect(1))));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                        new GainLifeEffect(1)));
    }
}
