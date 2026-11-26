package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "152")
public class AloeAlchemist extends Card {

    public AloeAlchemist() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{G}"))));
        addTrigger(Triggers.becomesPlotted(), BoostEffect.target(3, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
