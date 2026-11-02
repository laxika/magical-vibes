package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "77")
@CardRegistration(set = "SIR", collectorNumber = "84")
@CardRegistration(set = "MKC", collectorNumber = "114")
public class OngoingInvestigation extends Card {

    public OngoingInvestigation() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(null, CreateTokenEffect.ofClueToken(1), false, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        CreateTokenEffect.ofClueToken(1),
                        new GainLifeEffect(2)
                ),
                "{1}{G}, Exile a creature card from your graveyard: Investigate. You gain 2 life."
        ));
    }
}
