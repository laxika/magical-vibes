package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "153")
public class NyxWeaver extends Card {

    public NyxWeaver() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MillEffect(2, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(
                        new ExileSelfCost(),
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(null)
                ),
                "{1}{B}{G}, Exile this creature: Return target card from your graveyard to your hand."
        ));
    }
}
