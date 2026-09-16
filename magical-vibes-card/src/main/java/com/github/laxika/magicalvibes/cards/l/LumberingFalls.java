package com.github.laxika.magicalvibes.cards.l;

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

@CardRegistration(set = "EOS", collectorNumber = "24")
@CardRegistration(set = "EOS", collectorNumber = "69")
@CardRegistration(set = "EOS", collectorNumber = "114")
@CardRegistration(set = "EOS", collectorNumber = "159")
public class LumberingFalls extends Card {

    public LumberingFalls() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 3, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HEXPROOF),
                        Set.of(CardColor.GREEN, CardColor.BLUE))),
                "{2}{G}{U}: Until end of turn, this land becomes a 3/3 green and blue Elemental creature "
                        + "with hexproof. It's still a land."
        ));
    }
}
