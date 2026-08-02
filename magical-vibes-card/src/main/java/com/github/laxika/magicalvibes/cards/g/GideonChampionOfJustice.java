package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "13")
public class GideonChampionOfJustice extends Card {

    public GideonChampionOfJustice() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new PutCountersOnSelfEffect(
                        CounterType.LOYALTY,
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER),
                        true
                )),
                "+1: Put a loyalty counter on Gideon, Champion of Justice for each creature target opponent controls.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(
                                new CountersOnSource(CounterType.LOYALTY),
                                new CountersOnSource(CounterType.LOYALTY),
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(Keyword.INDESTRUCTIBLE),
                                null,
                                Set.of(),
                                GrantScope.SELF,
                                EffectDuration.UNTIL_END_OF_TURN,
                                null
                        ),
                        PreventDamageEffect.allToSelf()
                ),
                "0: Until end of turn, Gideon, Champion of Justice becomes an indestructible Human Soldier creature "
                        + "with power and toughness each equal to the number of loyalty counters on him. He's still "
                        + "a planeswalker. Prevent all damage that would be dealt to him this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -15,
                List.of(new ExileAllPermanentsEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "-15: Exile all other permanents."
        ));
    }
}
