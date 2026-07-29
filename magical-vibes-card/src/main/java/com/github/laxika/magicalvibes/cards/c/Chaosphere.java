package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MIR", collectorNumber = "164")
public class Chaosphere extends Card {

    public Chaosphere() {
        // Creatures with flying can block only creatures with flying.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                "Creatures with flying can block only creatures with flying"));

        // Creatures without flying have reach.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.REACH, GrantScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
