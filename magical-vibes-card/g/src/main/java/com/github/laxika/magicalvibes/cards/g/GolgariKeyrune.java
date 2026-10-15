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

@CardRegistration(set = "RTR", collectorNumber = "229")
public class GolgariKeyrune extends Card {

    public GolgariKeyrune() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        2, 2, List.of(CardSubtype.INSECT), Set.of(Keyword.DEATHTOUCH),
                        Set.of(CardColor.BLACK, CardColor.GREEN))),
                "{B}{G}: This artifact becomes a 2/2 black and green Insect artifact creature with deathtouch until end of turn."
        ));
    }
}
