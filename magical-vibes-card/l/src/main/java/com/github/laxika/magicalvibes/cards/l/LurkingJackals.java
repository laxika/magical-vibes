package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "62")
public class LurkingJackals extends Card {

    public LurkingJackals() {
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(controllerId))
                        .anyMatch(playerId -> gameData.getLife(playerId) <= 10)),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceMatchesPermanentPredicate(new PermanentIsEnchantmentPredicate())),
                new BecomeCreatureEffect(3, 2, CardSubtype.JACKAL));
    }
}
