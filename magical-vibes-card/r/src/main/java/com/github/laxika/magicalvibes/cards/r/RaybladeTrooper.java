package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "30")
public class RaybladeTrooper extends Card {

    private static final PermanentAllOfPredicate NONTOKEN_WITH_PLUS_ONE_COUNTER =
            new PermanentAllOfPredicate(List.of(
                    new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));

    public RaybladeTrooper() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        CreateTokenEffect humanSoldier = new CreateTokenEffect(
                1, "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(NONTOKEN_WITH_PLUS_ONE_COUNTER, humanSoldier));
        addEffect(EffectSlot.ON_DEATH,
                new TriggeringPermanentConditionalEffect(NONTOKEN_WITH_PLUS_ONE_COUNTER, humanSoldier));
    }
}
