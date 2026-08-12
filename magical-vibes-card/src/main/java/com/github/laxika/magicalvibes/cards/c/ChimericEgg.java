package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "106")
public class ChimericEgg extends Card {

    public ChimericEgg() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)),
                        List.of(new PutCountersOnSelfEffect(CounterType.CHARGE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.CHARGE),
                        new AnimatePermanentsEffect(
                                6, 6, List.of(CardSubtype.CONSTRUCT), Set.of(Keyword.TRAMPLE), null,
                                Set.of(CardType.ARTIFACT))
                ),
                "Remove three charge counters from this artifact: This artifact becomes a 6/6 Construct artifact creature with trample until end of turn."
        ));
    }
}
