package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "DTK", collectorNumber = "242")
public class OjutaiMonument extends Card {

    public OjutaiMonument() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {W} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{U}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING),
                        Set.of(CardColor.WHITE, CardColor.BLUE))),
                "{4}{W}{U}: This artifact becomes a 4/4 white and blue Dragon artifact creature with flying until end of turn."
        ));
    }
}
