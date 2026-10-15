package com.github.laxika.magicalvibes.cards.d;

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

@CardRegistration(set = "DTK", collectorNumber = "238")
public class DromokaMonument extends Card {

    public DromokaMonument() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.GREEN))),
                "{T}: Add {G} or {W}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{W}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING),
                        Set.of(CardColor.GREEN, CardColor.WHITE))),
                "{4}{G}{W}: This artifact becomes a 4/4 green and white Dragon artifact creature with flying until end of turn."
        ));
    }
}
