package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.NecropotenceSetAsideTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "182")
@CardRegistration(set = "ICE", collectorNumber = "154")
public class Necropotence extends Card {

    public Necropotence() {
        // Skip your draw step.
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());

        // Whenever you discard a card, exile that card from your graveyard.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new ExileDiscardedCardFromGraveyardEffect());

        // Pay 1 life: Exile the top card of your library face down. Put that card into your hand at
        // the beginning of your next end step.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new NecropotenceSetAsideTopCardEffect()),
                "Pay 1 life: Exile the top card of your library face down. Put that card into your hand at the beginning of your next end step."));
    }
}
