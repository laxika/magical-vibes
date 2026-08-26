package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "284")
public class RestlessVents extends Card {

    public RestlessVents() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 3, List.of(CardSubtype.INSECT), Set.of(Keyword.MENACE),
                        Set.of(CardColor.BLACK, CardColor.RED))),
                "{1}{B}{R}: Until end of turn, this land becomes a 2/3 black and red Insect creature "
                        + "with menace. It's still a land."
        ));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"
        ));
    }
}
