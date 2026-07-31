package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "32")
public class PhantasmalSphere extends Card {

    public PhantasmalSphere() {
        // At the beginning of your upkeep, put a +1/+1 counter on this creature, then sacrifice
        // this creature unless you pay {1} for each +1/+1 counter on it. One sequence, so the
        // counter lands before the payment is sized (two slot effects would be two stack entries).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new DestroyUnlessPaysPerCounterEffect(CounterType.PLUS_ONE_PLUS_ONE, "{1}", true)
        ));

        // When this creature leaves the battlefield, target opponent creates an X/X blue Orb
        // creature token with flying, where X is the number of +1/+1 counters on this creature.
        // X is frozen from the leaving permanent when the trigger is collected.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Orb",
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        CardColor.BLUE, List.of(CardSubtype.ORB),
                        Set.of(Keyword.FLYING), Set.of())
        ));
    }
}
