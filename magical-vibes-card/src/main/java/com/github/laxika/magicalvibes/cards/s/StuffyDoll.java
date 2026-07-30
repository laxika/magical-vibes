package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "218")
public class StuffyDoll extends Card {

    public StuffyDoll() {
        // As this creature enters, choose a player. Whenever this creature is dealt damage,
        // it deals that much damage to the chosen player. The chosen player is the controller's
        // opponent (single-opponent model, as in Booby Trap / Cursed Rack); the damage amount
        // snapshots onto the trigger's eventValue.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT));

        // {T}: This creature deals 1 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(new DealDamageToSourceEffect(1)),
                "{T}: Stuffy Doll deals 1 damage to itself."
        ));
    }
}
