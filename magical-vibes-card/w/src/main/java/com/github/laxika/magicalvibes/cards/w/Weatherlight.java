package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "237")
@CardRegistration(set = "SLD", collectorNumber = "2100")
@CardRegistration(set = "NEC", collectorNumber = "165")
public class Weatherlight extends Card {

    public Weatherlight() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5, new CardIsHistoricPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
