package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "227")
public class BorosKeyrune extends Card {

    public BorosKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE))),
                "{T}: Add {R} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        1, 1, List.of(CardSubtype.SOLDIER), Set.of(Keyword.DOUBLE_STRIKE),
                        Set.of(CardColor.RED, CardColor.WHITE))),
                "{R}{W}: This artifact becomes a 1/1 red and white Soldier artifact creature with double strike until end of turn."
        ));
    }
}
