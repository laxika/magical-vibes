package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "216")
public class SlaughterSinger extends Card {

    public SlaughterSinger() {
        addStaticEffect(new ToxicEffect(2));
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentHasKeywordPredicate(Keyword.TOXIC),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                new BoostTargetCreatureEffect(1, 1));
    }
}
