package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "9")
public class BasrisLieutenant extends Card {

    public BasrisLieutenant() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, createsKnightIfHadPlusOnePlusOneCounter());
        addEffect(EffectSlot.ON_DEATH, createsKnightIfHadPlusOnePlusOneCounter());
    }

    private static TriggeringPermanentConditionalEffect createsKnightIfHadPlusOnePlusOneCounter() {
        return new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())
        );
    }
}
