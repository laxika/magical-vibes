package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "162")
public class MikokoroCenterOfTheSea extends Card {

    public MikokoroCenterOfTheSea() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: Each player draws a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new EachPlayerDrawsCardEffect(1)),
                "{2}, {T}: Each player draws a card."
        ));
    }
}
