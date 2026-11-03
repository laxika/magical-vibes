package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "212")
@CardRegistration(set = "SLD", collectorNumber = "1078")
@CardRegistration(set = "UMA", collectorNumber = "189")
@CardRegistration(set = "2XM", collectorNumber = "185")
public class Vengevine extends Card {

    public Vengevine() {
        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.CREATURE))
                        .nthThisTurn(2), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Return Vengevine from your graveyard to the battlefield?"));
    }
}
