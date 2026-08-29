package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "121")
public class Spiritmonger extends Card {

    public Spiritmonger() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new PutCountersOnSourceEffect(1, 1, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(false, false)),
                "{G}: This creature becomes the color of your choice until end of turn."
        ));
    }
}
