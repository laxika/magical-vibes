package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "4ED", collectorNumber = "126")
@CardRegistration(set = "LEG", collectorNumber = "92")
@CardRegistration(set = "ME3", collectorNumber = "61")
public class CosmicHorror extends Card {

    public CosmicHorror() {
        // At the beginning of your upkeep, destroy this creature unless you pay {3}{B}{B}{B}.
        // If this creature is destroyed this way, it deals 7 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{3}{B}{B}{B}", SequenceEffect.of(new DestroyTargetPermanentEffect(Subjects.source()), new ConditionalEffect(HappenedThisWay.destroyed(),
                                DealDamageEffect.to(Subjects.you(), 7)))));
    }
}
