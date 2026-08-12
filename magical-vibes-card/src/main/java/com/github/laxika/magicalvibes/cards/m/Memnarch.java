package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "129")
public class Memnarch extends Card {

    public Memnarch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT, true)),
                "{1}{U}{U}: Target permanent becomes an artifact in addition to its other types.",
                TargetFilters.permanent()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "{3}{U}: Gain control of target artifact.",
                TargetFilters.artifact()
        ));
    }
}
