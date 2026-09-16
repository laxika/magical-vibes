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

@CardRegistration(set = "PIO", collectorNumber = "275")
@CardRegistration(set = "EOS", collectorNumber = "38")
@CardRegistration(set = "EOS", collectorNumber = "83")
@CardRegistration(set = "EOS", collectorNumber = "128")
@CardRegistration(set = "EOS", collectorNumber = "173")
public class ShamblingVent extends Card {

    public ShamblingVent() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{B}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 3, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.LIFELINK),
                        Set.of(CardColor.WHITE, CardColor.BLACK))),
                "{1}{W}{B}: Until end of turn, this land becomes a 2/3 white and black Elemental "
                        + "creature with lifelink. It's still a land."
        ));
    }
}
