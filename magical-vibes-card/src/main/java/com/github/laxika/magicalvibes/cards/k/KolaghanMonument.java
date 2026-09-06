package com.github.laxika.magicalvibes.cards.k;

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

@CardRegistration(set = "DTK", collectorNumber = "241")
public class KolaghanMonument extends Card {

    public KolaghanMonument() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{R}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING),
                        Set.of(CardColor.BLACK, CardColor.RED))),
                "{4}{B}{R}: This artifact becomes a 4/4 black and red Dragon artifact creature with flying until end of turn."
        ));
    }
}
