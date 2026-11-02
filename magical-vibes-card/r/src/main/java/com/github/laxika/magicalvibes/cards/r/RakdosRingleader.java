package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "186")
public class RakdosRingleader extends Card {

    public RakdosRingleader() {
        // First strike comes from the Scryfall keyword data.
        // Whenever this creature deals combat damage to a player, that player discards a card at random.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate Rakdos Ringleader."
        ));
    }
}
