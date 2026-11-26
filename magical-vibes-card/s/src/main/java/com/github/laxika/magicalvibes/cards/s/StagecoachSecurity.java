package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "30")
public class StagecoachSecurity extends Card {

    public StagecoachSecurity() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                BoostEffect.creaturesYouControl(1, 1),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.creaturesYouControl()));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{W}"))));
    }
}
