package com.github.laxika.magicalvibes.cards.a;

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

@CardRegistration(set = "RTR", collectorNumber = "225")
public class AzoriusKeyrune extends Card {

    public AzoriusKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE))),
                "{T}: Add {W} or {U}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 2, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING),
                        Set.of(CardColor.WHITE, CardColor.BLUE))),
                "{W}{U}: This artifact becomes a 2/2 white and blue Bird artifact creature with flying until end of turn."
        ));
    }
}
