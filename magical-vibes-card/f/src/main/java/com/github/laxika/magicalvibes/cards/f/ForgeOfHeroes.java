package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "58")
@CardRegistration(set = "FIC", collectorNumber = "395")
public class ForgeOfHeroes extends Card {

    public ForgeOfHeroes() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        var commanderEnteredThisTurn = new PermanentAllOfPredicate(List.of(
                new PermanentIsCommanderPredicate(),
                new PermanentEnteredBattlefieldThisTurnPredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentIsCreaturePredicate()),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentIsPlaneswalkerPredicate()),
                                new PutCounterOnTargetPermanentEffect(CounterType.LOYALTY))
                ),
                "{T}: Choose target commander that entered the battlefield this turn. Put a +1/+1 counter on it if it's a creature and a loyalty counter on it if it's a planeswalker.",
                new PermanentPredicateTargetFilter(
                        commanderEnteredThisTurn,
                        "Target must be a commander that entered the battlefield this turn"
                )
        ));
    }
}
