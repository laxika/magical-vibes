package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "6")
@CardRegistration(set = "40K", collectorNumber = "172")
public class BelakorTheDarkMaster extends Card {

    public BelakorTheDarkMaster() {
        PermanentCount demons = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.DEMON), CountScope.CONTROLLER);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(demons),
                new LoseLifeEffect(demons, LoseLifeRecipient.CONTROLLER));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DEMON))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()));
    }
}
