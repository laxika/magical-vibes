package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "108")
public class VeiledCrocodile extends Card {

    public VeiledCrocodile() {
        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .anyMatch(playerId -> gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()),
                new PermanentIsEnchantmentPredicate(),
                null, 0, 0, null,
                List.of(new BecomeCreatureEffect(4, 4, CardSubtype.CROCODILE)),
                "Veiled Crocodile's state-triggered ability"
        ));
    }
}
