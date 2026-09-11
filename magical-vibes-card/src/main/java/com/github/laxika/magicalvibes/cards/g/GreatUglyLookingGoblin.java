package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ClapSnap;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "HOB", collectorNumber = "74")
public class GreatUglyLookingGoblin extends Card {

    public GreatUglyLookingGoblin() {
        setBackFaceCard(new ClapSnap());
        addCastingOption(new AdventureCast("{1}{B}"));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ClapSnap";
    }
}
