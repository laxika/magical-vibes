package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "62")
public class LurkingJackals extends Card {

    public LurkingJackals() {
        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(controllerId))
                        .anyMatch(playerId -> gameData.getLife(playerId) <= 10),
                new PermanentIsEnchantmentPredicate(), null, 0, 0, null,
                List.of(new ConditionalEffect(new AnyPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsSourcePermanentPredicate(), new PermanentIsEnchantmentPredicate()))),
                        new BecomeCreatureEffect(3, 2, CardSubtype.JACKAL))),
                "Lurking Jackals's state-triggered ability"
        ));
    }
}
