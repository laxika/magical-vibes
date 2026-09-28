package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAdjacentToSourcePredicate;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "276")
@CardRegistration(set = "MB2", collectorNumber = "512")
public class DefenderOfTheQueue extends Card {

    public DefenderOfTheQueue() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, Set.of(Keyword.VIGILANCE), GrantScope.OWN_CREATURES,
                new PermanentAdjacentToSourcePredicate()));
    }
}
