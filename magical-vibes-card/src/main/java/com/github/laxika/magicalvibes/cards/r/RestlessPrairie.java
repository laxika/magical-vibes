package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "281")
public class RestlessPrairie extends Card {

    public RestlessPrairie() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                1, 1, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE))),
                "{T}: Add {G} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 3, List.of(CardSubtype.LLAMA), Set.of(),
                        Set.of(CardColor.GREEN, CardColor.WHITE))),
                "{2}{G}{W}: This land becomes a 3/3 green and white Llama creature until end of turn. "
                        + "It's still a land."
        ));
    }
}
