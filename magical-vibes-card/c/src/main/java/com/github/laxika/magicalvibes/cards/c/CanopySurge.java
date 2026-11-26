package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "INV", collectorNumber = "184")
public class CanopySurge extends Card {

    public CanopySurge() {
        addStaticEffect(new KickerEffect("{2}"));
        addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                DealDamageEffect.toEachCreature(1,
                        new PermanentHasKeywordPredicate(Keyword.FLYING)).andTo(Subjects.eachPlayer()),
                DealDamageEffect.toEachCreature(4,
                        new PermanentHasKeywordPredicate(Keyword.FLYING)).andTo(Subjects.eachPlayer())
        ));
    }
}
