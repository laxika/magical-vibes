package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "228")
public class DimirKeyrune extends Card {

    public DimirKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK))),
                "{T}: Add {U} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(
                        AnimatePermanentsEffect.withAnimatedColors(
                                2, 2, List.of(CardSubtype.HORROR), Set.of(),
                                Set.of(CardColor.BLUE, CardColor.BLACK)),
                        new MakeCreatureUnblockableEffect(true)),
                "{U}{B}: This artifact becomes a 2/2 blue and black Horror artifact creature until end of turn and can't be blocked this turn."
        ));
    }
}
