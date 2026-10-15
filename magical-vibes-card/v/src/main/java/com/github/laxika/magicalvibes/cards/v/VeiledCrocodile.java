package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "108")
public class VeiledCrocodile extends Card {

    public VeiledCrocodile() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> sourcePermanent.getCard().hasType(CardType.ENCHANTMENT)
                        && gameData.orderedPlayerIds.stream()
                        .anyMatch(playerId -> gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()),
                List.of(new BecomeCreatureEffect(4, 4, CardSubtype.CROCODILE)),
                "Veiled Crocodile's state-triggered ability"
        ));
    }
}
