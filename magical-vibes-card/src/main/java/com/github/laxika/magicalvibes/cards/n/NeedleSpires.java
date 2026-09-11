package com.github.laxika.magicalvibes.cards.n;

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
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "175")
public class NeedleSpires extends Card {

    public NeedleSpires() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {R} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 1, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.DOUBLE_STRIKE),
                        Set.of(CardColor.RED, CardColor.WHITE))),
                "{2}{R}{W}: Until end of turn, Needle Spires becomes a 2/1 red and white Elemental creature "
                        + "with double strike. It's still a land."
        ));
    }
}
