package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "GTC", collectorNumber = "237")
public class SimicKeyrune extends Card {

    public SimicKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{U}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 3, List.of(CardSubtype.CRAB), Set.of(Keyword.HEXPROOF),
                        Set.of(CardColor.GREEN, CardColor.BLUE))),
                "{G}{U}: This artifact becomes a 2/3 green and blue Crab artifact creature with hexproof until end of turn."
        ));
    }
}
