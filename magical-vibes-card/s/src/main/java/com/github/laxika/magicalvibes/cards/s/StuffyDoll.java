package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "218")
@CardRegistration(set = "TSP", collectorNumber = "264")
@CardRegistration(set = "SLD", collectorNumber = "880")
@CardRegistration(set = "TSR", collectorNumber = "274")
public class StuffyDoll extends Card {

    public StuffyDoll() {
        addStaticEffect(new ChoosePlayerOnEnterEffect());

        addTrigger(Triggers.dealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER));

        // {T}: This creature deals 1 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(new DealDamageToSourceEffect(1)),
                "{T}: Stuffy Doll deals 1 damage to itself."
        ));
    }
}
