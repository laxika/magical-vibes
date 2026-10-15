package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "GTC", collectorNumber = "230")
public class GruulKeyrune extends Card {

    public GruulKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN))),
                "{T}: Add {R} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 2, List.of(CardSubtype.BEAST), Set.of(Keyword.TRAMPLE),
                        Set.of(CardColor.RED, CardColor.GREEN))),
                "{R}{G}: This artifact becomes a 3/2 red and green Beast artifact creature with trample until end of turn."
        ));
    }
}
