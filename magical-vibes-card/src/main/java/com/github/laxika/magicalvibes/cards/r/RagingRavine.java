package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "141")
public class RagingRavine extends Card {

    public RagingRavine() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN))),
                "{T}: Add {R} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(
                        AnimatePermanentsEffect.withAnimatedColors(
                                3, 3, List.of(CardSubtype.ELEMENTAL), Set.of(),
                                Set.of(CardColor.RED, CardColor.GREEN)),
                        new GrantEffectToSourceUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))
                ),
                "{2}{R}{G}: Until end of turn, this land becomes a 3/3 red and green Elemental creature "
                        + "with \"Whenever this creature attacks, put a +1/+1 counter on it.\" It's still a land."
        ));
    }
}
