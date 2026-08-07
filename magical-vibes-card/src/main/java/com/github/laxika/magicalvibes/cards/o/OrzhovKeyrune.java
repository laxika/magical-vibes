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

@CardRegistration(set = "GTC", collectorNumber = "233")
public class OrzhovKeyrune extends Card {

    public OrzhovKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{B}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        1, 4, List.of(CardSubtype.THRULL), Set.of(Keyword.LIFELINK),
                        Set.of(CardColor.WHITE, CardColor.BLACK))),
                "{W}{B}: This artifact becomes a 1/4 white and black Thrull artifact creature with lifelink until end of turn."
        ));
    }
}
