package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "APC", collectorNumber = "86")
public class StrengthOfNight extends Card {

    public StrengthOfNight() {
        addStaticEffect(new KickerEffect("{B}"));
        addSpellEffect(BoostEffect.creaturesYouControl(1, 1));
        addSpellEffect(new ConditionalEffect(new Kicked(),
                BoostEffect.creaturesYouControl(2, 2, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))));
    }
}
