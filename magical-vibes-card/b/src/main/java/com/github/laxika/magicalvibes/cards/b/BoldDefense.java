package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ZEN", collectorNumber = "3")
public class BoldDefense extends Card {

    public BoldDefense() {
        addStaticEffect(new KickerEffect("{3}{W}"));
        addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                BoostEffect.creaturesYouControl(1, 1),
                SequenceEffect.of(
                        BoostEffect.creaturesYouControl(2, 2),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.otherCreaturesYouControl())
                )));
    }
}
