package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "152")
public class BladesOfVelisVel extends Card {

    public BladesOfVelisVel() {
        // Up to two target creatures each get +2/+0 and gain all creature types
        // (Changeling) until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET));
    }
}
