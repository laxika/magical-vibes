package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "134")
public class Peppersmoke extends Card {

    public Peppersmoke() {
        // Target creature gets -1/-1 until end of turn. (target auto-derived)
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        // If you control a Faerie, draw a card.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FAERIE)),
                new DrawCardEffect(1)));
    }
}
