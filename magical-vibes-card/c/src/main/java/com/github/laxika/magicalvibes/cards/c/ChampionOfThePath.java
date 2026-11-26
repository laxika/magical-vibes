package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;

@CardRegistration(set = "ECL", collectorNumber = "130")
@CardRegistration(set = "ECL", collectorNumber = "362")
public class ChampionOfThePath extends Card {

    public ChampionOfThePath() {
        addCastingCost(new BeholdAndExileCost(CardSubtype.ELEMENTAL));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), new TriggeringPermanentPower())
                        .byTriggeringPermanent());
    }
}
