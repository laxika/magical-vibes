package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenIfDyingSourceHadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "14")
@CardRegistration(set = "ECC", collectorNumber = "34")
public class VillagePillagers extends Card {

    public VillagePillagers() {
        PermanentPredicate opponentsCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToEachMatchingPermanentEffect(1, opponentsCreatures, EachPermanentScope.ALL_PLAYERS));
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new CreateTokenIfDyingSourceHadCounterEffect(
                        CounterType.ANY, CreateTokenEffect.ofTreasureToken(1, true)));
    }
}
