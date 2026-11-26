package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "65")
public class KeldonMantle extends Card {

    public KeldonMantle() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate enchanted creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.of(Subjects.attached(), new Fixed(1), new Fixed(0))),
                "{R}: Enchanted creature gets +1/+0 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature())),
                "{G}: Enchanted creature gains trample until end of turn."
        ));
    }
}
