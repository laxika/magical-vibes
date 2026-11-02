package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "243")
@CardRegistration(set = "SOC", collectorNumber = "344")
@CardRegistration(set = "MSC", collectorNumber = "284")
public class ContainmentConstruct extends Card {

    public ContainmentConstruct() {
        addTrigger(Triggers.youDiscardCard(), new MayEffect(
                ExileDiscardedCardFromGraveyardEffect.withPlayPermission(),
                "Exile that card from your graveyard?"));
    }
}
