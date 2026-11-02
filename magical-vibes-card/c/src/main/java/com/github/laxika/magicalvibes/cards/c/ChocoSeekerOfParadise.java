package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "215")
@CardRegistration(set = "FIN", collectorNumber = "479")
@CardRegistration(set = "FIN", collectorNumber = "569")
public class ChocoSeekerOfParadise extends Card {

    public ChocoSeekerOfParadise() {
        addTrigger(Triggers.youAttack(),
                new ConditionalEffect(new com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers(
                        1, new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                        new LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect(
                                new EventValue())));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(1, 0));
    }
}
