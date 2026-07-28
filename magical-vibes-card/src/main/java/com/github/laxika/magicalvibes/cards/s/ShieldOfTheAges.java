package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "335")
public class ShieldOfTheAges extends Card {

    public ShieldOfTheAges() {
        // {2}: Prevent the next 1 damage that would be dealt to you this turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(PreventDamageEffect.nextToController(1)),
                "{2}: Prevent the next 1 damage that would be dealt to you this turn."));
    }
}
