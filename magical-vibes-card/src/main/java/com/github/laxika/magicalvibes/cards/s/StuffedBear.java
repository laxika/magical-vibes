package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "259")
public class StuffedBear extends Card {

    public StuffedBear() {
        // {2}: This artifact becomes a 4/4 green Bear artifact creature until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.BEAR), Set.of(), CardColor.GREEN)),
                "{2}: This artifact becomes a 4/4 green Bear artifact creature until end of turn."
        ));
    }
}
