package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MBS", collectorNumber = "73")
public class RallyTheForces extends Card {

    public RallyTheForces() {
        // Attacking creatures get +1/+0 and gain first strike until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
