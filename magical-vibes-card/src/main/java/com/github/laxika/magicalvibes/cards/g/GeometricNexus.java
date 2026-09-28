package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEqualToTriggeringSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "77")
public class GeometricNexus extends Card {

    public GeometricNexus() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        List.of(new PutCountersOnSelfEqualToTriggeringSpellManaValueEffect(CounterType.CHARGE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(
                        new RemoveAllCountersAsCostEffect(CounterType.CHARGE),
                        new CreateTokenEffect(
                                "Fractal", 0, 0,
                                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                                List.of(CardSubtype.FRACTAL)),
                        new PutCountersOnCreatedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())
                ),
                "{6}, {T}, Remove all charge counters from this artifact: Create a 0/0 green and blue Fractal creature token. Put X +1/+1 counters on it, where X is the number of charge counters removed this way."
        ));
    }
}
