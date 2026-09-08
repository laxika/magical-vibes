package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "171")
public class HissingQuagmire extends Card {

    public HissingQuagmire() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 2, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.DEATHTOUCH),
                        Set.of(CardColor.BLACK, CardColor.GREEN))),
                "{1}{B}{G}: Until end of turn, this land becomes a 2/2 black and green Elemental creature "
                        + "with deathtouch. It's still a land."
        ));
    }
}
