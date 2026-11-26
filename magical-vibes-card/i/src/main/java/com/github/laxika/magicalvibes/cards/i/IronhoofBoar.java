package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "148")
public class IronhoofBoar extends Card {

    public IronhoofBoar() {
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        BoostEffect.target(3, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())),
                "Channel — {1}{R}, Discard this card: Target creature gets +3/+1 and gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
