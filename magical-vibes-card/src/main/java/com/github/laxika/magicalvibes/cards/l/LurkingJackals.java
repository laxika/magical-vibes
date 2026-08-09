package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "62")
public class LurkingJackals extends Card {

    public LurkingJackals() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> sourcePermanent.getCard().hasType(CardType.ENCHANTMENT)
                        && gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(controllerId))
                        .anyMatch(playerId -> gameData.getLife(playerId) <= 10),
                List.of(new BecomeCreatureEffect(3, 2, CardSubtype.JACKAL)),
                "Lurking Jackals's state-triggered ability"
        ));
    }
}
