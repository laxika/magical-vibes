package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "6")
public class CaptainOfTheWatch extends Card {

    public CaptainOfTheWatch() {
        // Other Soldier creatures you control get +1/+1 and have vigilance.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER))));

        // When Captain of the Watch enters the battlefield, create three 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateCreatureTokenEffect(
                3, "Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
