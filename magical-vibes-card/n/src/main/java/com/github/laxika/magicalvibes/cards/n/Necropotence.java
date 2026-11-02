package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.NecropotenceSetAsideTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "182")
@CardRegistration(set = "ICE", collectorNumber = "154")
@CardRegistration(set = "DKM", collectorNumber = "7")
@CardRegistration(set = "V09", collectorNumber = "9")
@CardRegistration(set = "ME2", collectorNumber = "107")
@CardRegistration(set = "IMA", collectorNumber = "98")
@CardRegistration(set = "EMA", collectorNumber = "98")
@CardRegistration(set = "VMA", collectorNumber = "130")
@CardRegistration(set = "WOT", collectorNumber = "31")
@CardRegistration(set = "WOT", collectorNumber = "74")
@CardRegistration(set = "WOT", collectorNumber = "94")
public class Necropotence extends Card {

    public Necropotence() {
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());

        // Whenever you discard a card, exile that card from your graveyard.
        addTrigger(Triggers.youDiscardCard(), new ExileDiscardedCardFromGraveyardEffect());

        // Pay 1 life: Exile the top card of your library face down. Put that card into your hand at
        // the beginning of your next end step.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new NecropotenceSetAsideTopCardEffect()),
                "Pay 1 life: Exile the top card of your library face down. Put that card into your hand at the beginning of your next end step."));
    }
}
