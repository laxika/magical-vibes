package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "15")
public class OpalAvenger extends Card {

    public OpalAvenger() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) <= 10,
                new PermanentIsEnchantmentPredicate(), null, 0, null,
                List.of(new BecomeCreatureEffect(3, 5, CardSubtype.SOLDIER)),
                "Opal Avenger's state-triggered ability"
        ));
    }
}
