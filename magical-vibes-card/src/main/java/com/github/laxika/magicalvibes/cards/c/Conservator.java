package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "309")
public class Conservator extends Card {

    public Conservator() {
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(PreventDamageEffect.nextToController(2)),
                "{3}, {T}: Prevent the next 2 damage that would be dealt to you this turn."));
    }
}
