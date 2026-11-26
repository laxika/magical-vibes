package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "165")
public class BrambleguardVeteran extends Card {

    public BrambleguardVeteran() {
        addTrigger(Triggers.youCastSpell().expend(4),
                SequenceEffect.of(
                        BoostEffect.creaturesYouControl(1, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.RACCOON)),
                        new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.creaturesYouControl(),
                                new PermanentHasSubtypePredicate(CardSubtype.RACCOON))
                ));
    }
}
