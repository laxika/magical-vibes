package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "108")
public class DarksteelBrute extends Card {

    public DarksteelBrute() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AnimatePermanentsEffect(2, 2, List.of(CardSubtype.BEAST), Set.of())),
                "{3}: This artifact becomes a 2/2 Beast artifact creature until end of turn."
        ));
    }
}
