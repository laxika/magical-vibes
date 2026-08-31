package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "195")
public class UpTheBeanstalk extends Card {

    public UpTheBeanstalk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(5),
                List.of(new DrawCardEffect())));
    }
}
