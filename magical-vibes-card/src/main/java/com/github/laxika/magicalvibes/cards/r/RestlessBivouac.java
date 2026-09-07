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
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "257")
public class RestlessBivouac extends Card {

    public RestlessBivouac() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {R} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 2, List.of(CardSubtype.OX), Set.of(),
                        Set.of(CardColor.RED, CardColor.WHITE))),
                "{1}{R}{W}: This land becomes a 2/2 red and white Ox creature until end of turn. "
                        + "It's still a land."
        ));
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
