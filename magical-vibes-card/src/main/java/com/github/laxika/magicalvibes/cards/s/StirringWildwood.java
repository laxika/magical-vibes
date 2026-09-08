package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "WWK", collectorNumber = "144")
public class StirringWildwood extends Card {

    public StirringWildwood() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE))),
                "{T}: Add {G} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 4, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.REACH),
                        Set.of(CardColor.GREEN, CardColor.WHITE))),
                "{1}{G}{W}: Until end of turn, this land becomes a 3/4 green and white Elemental creature "
                        + "with reach. It's still a land."
        ));
    }
}
