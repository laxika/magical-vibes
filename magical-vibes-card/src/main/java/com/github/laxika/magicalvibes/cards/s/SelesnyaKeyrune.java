package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "233")
public class SelesnyaKeyrune extends Card {

    public SelesnyaKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE))),
                "{T}: Add {G} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 3, List.of(CardSubtype.WOLF), Set.of(),
                        Set.of(CardColor.GREEN, CardColor.WHITE))),
                "{G}{W}: This artifact becomes a 3/3 green and white Wolf artifact creature until end of turn."
        ));
    }
}
