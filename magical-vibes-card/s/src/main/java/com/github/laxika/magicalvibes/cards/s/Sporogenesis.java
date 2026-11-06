package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "USG", collectorNumber = "273")
public class Sporogenesis extends Card {

    public Sporogenesis() {
        var nontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));

        target(new PermanentPredicateTargetFilter(nontokenCreature, "Target must be a nontoken creature"))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.FUNGUS, 1,
                                nontokenCreature),
                        "Put a fungus counter on the target creature?"));

        var saprolingsForFungusCounters = new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                                .withAmount(new CountersOnTriggeringPermanent(CounterType.FUNGUS));
        addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.FUNGUS),
                ObjectRelation.ANY), saprolingsForFungusCounters);

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new RemoveAllCountersFromMatchingPermanentsEffect(
                        CounterType.FUNGUS, new PermanentIsCreaturePredicate()));
    }
}
