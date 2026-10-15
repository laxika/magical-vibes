package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "168")
public class CrimsonRoc extends Card {

    public CrimsonRoc() {
        // Whenever this creature blocks a creature without flying,
        // this creature gets +1/+0 and gains first strike until end of turn.
        PermanentNotPredicate withoutFlying = new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenCombatOpponentMatchesEffect(
                withoutFlying, 1, 0, Set.of(Keyword.FIRST_STRIKE)));
    }
}
