package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "89")
public class DrudgeSentinel extends Card {

    public DrudgeSentinel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new TapPermanentsEffect(Subjects.source()), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())),
                "{3}: Tap Drudge Sentinel. It gains indestructible until end of turn."
        ));
    }
}
