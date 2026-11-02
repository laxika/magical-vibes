package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "155")
@CardRegistration(set = "C17", collectorNumber = "176")
public class KolaghanTheStormsFury extends Card {

    public KolaghanTheStormsFury() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{B}{R}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnSelfToHandIfDashCostPaidEffect());

        // Whenever a Dragon you control attacks, creatures you control get +1/+0 until end of turn.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        new BoostAllOwnCreaturesEffect(1, 0)));
    }
}
