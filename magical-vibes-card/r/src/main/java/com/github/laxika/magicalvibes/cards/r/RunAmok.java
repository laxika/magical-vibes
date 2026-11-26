package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "140")
@CardRegistration(set = "KHM", collectorNumber = "147")
@CardRegistration(set = "TLE", collectorNumber = "258")
@CardRegistration(set = "TLE", collectorNumber = "284")
public class RunAmok extends Card {

    public RunAmok() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(BoostEffect.target(3, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
