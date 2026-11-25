package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "99")
public class LingeringPhantom extends Card {

    public LingeringPhantom() {
        // Whenever you cast a historic spell, you may pay {B}. If you do,
        // return Lingering Phantom from your graveyard to your hand.
        // (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                UnlessPaysEffect.youMayPay("{B}", ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()).withPrompt("Pay {B} to return Lingering Phantom from your graveyard to your hand?"));
    }
}
