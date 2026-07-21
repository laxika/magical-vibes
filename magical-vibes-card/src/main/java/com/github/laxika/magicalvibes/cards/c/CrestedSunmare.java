package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "6")
public class CrestedSunmare extends Card {

    public CrestedSunmare() {
        // Other Horses you control have indestructible.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES, new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.HORSE))));

        // At the beginning of each end step, if you gained life this turn, create a 5/5 white Horse creature token.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new CreateTokenEffect("Horse", 5, 5, CardColor.WHITE,
                        List.of(CardSubtype.HORSE), Set.of(), Set.of())));
    }
}
