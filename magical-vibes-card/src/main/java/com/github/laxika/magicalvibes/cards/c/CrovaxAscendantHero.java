package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "3")
public class CrovaxAscendantHero extends Card {

    public CrovaxAscendantHero() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                ))));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.WHITE)))));

        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), ReturnToHandEffect.self()),
                "Pay 2 life: Return Crovax to its owner's hand."));
    }
}
