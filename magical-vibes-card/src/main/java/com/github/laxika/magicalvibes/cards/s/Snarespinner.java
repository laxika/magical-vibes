package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "M21", collectorNumber = "207")
public class Snarespinner extends Card {

    public Snarespinner() {
        // Whenever this creature blocks a creature with flying, this creature gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenCombatOpponentMatchesEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING), 2, 0));
    }
}
