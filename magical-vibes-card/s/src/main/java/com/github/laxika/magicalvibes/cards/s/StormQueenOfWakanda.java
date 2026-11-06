package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "97")
@CardRegistration(set = "MSC", collectorNumber = "418")
public class StormQueenOfWakanda extends Card {

    public StormQueenOfWakanda() {
        PermanentPredicate anotherAttackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                anotherAttackingCreature,
                "Target must be another attacking creature"
        ))
                .addTrigger(Triggers.attacks(),
                        new BoostTargetCreatureEffect(new SourcePower(), new Fixed(0), anotherAttackingCreature),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, anotherAttackingCreature));

        addTrigger(Triggers.creatureAttacksYou().withFilter(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                new DealDamageToTriggeringAttackerEffect(new SourcePower()));
    }
}
