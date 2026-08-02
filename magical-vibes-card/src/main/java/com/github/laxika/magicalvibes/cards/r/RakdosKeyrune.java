package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "RTR", collectorNumber = "232")
public class RakdosKeyrune extends Card {

    public RakdosKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 1, List.of(CardSubtype.DEVIL), Set.of(Keyword.FIRST_STRIKE),
                        Set.of(CardColor.BLACK, CardColor.RED))),
                "{B}{R}: Until end of turn, this artifact becomes a 3/1 black and red Devil artifact creature with first strike."
        ));
    }
}
