package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "221")
public class OranRiefTheVastwood extends Card {

    public OranRiefTheVastwood() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                                new PermanentEnteredBattlefieldThisTurnPredicate()
                        )),
                        EachPermanentScope.ALL_PLAYERS
                )),
                "{T}: Put a +1/+1 counter on each green creature that entered this turn."
        ));
    }
}
