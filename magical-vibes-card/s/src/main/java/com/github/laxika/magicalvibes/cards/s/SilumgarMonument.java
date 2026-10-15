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

@CardRegistration(set = "DTK", collectorNumber = "243")
public class SilumgarMonument extends Card {

    public SilumgarMonument() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK))),
                "{T}: Add {U} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{B}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING),
                        Set.of(CardColor.BLUE, CardColor.BLACK))),
                "{4}{U}{B}: This artifact becomes a 4/4 blue and black Dragon artifact creature with flying until end of turn."
        ));
    }
}
