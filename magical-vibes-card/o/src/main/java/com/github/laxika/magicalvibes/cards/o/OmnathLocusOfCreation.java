package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "232")
@CardRegistration(set = "SLD", collectorNumber = "1686")
public class OmnathLocusOfCreation extends Card {

    public OmnathLocusOfCreation() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), ConditionalEffect.unless(
                new NthAbilityResolutionThisTurn(1), new GainLifeEffect(4)),
                ConditionalEffect.unless(
                new NthAbilityResolutionThisTurn(2), SequenceEffect.of(
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN),
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE))),
                ConditionalEffect.unless(
                new NthAbilityResolutionThisTurn(3), SequenceEffect.of(
                        new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT),
                        new DealDamageToEachMatchingPermanentEffect(4,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsPlaneswalkerPredicate(),
                                        new PermanentNotPredicate(
                                                new PermanentControlledBySourceControllerPredicate()))),
                                EachPermanentScope.ALL_PLAYERS))));
    }
}
