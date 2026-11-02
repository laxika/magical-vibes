package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "38")
@CardRegistration(set = "FIC", collectorNumber = "143")
@CardRegistration(set = "FIC", collectorNumber = "467")
public class LuluSternGuardian extends Card {

    public LuluSternGuardian() {
        target(new PermanentPredicateTargetFilter(new PermanentIsAttackingSourceControllerPredicate(),
                "Target must be a creature attacking you"))
                .addTrigger(Triggers.creaturesAttackYou(),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ProliferateEffect()),
                "{3}{U}: Proliferate."
        ));
    }
}
