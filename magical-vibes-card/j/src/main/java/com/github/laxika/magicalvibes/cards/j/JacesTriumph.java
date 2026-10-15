package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WAR", collectorNumber = "55")
public class JacesTriumph extends Card {

    public JacesTriumph() {
        ControlsPermanent controlsJace = new ControlsPermanent(
                new PermanentHasSubtypePredicate(CardSubtype.JACE));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(controlsJace, new DrawCardEffect(3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(controlsJace), new DrawCardEffect(2)));
    }
}
