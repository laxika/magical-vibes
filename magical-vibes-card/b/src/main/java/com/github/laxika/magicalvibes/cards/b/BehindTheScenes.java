package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "100")
@CardRegistration(set = "TDC", collectorNumber = "172")
public class BehindTheScenes extends Card {

    public BehindTheScenes() {
        addStaticEffect(new GrantKeywordEffect(Keyword.SKULK, Subjects.creaturesYouControl()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(BoostEffect.creaturesYouControl(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
