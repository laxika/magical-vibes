package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "248")
public class DiaryOfDreams extends Card {

    public DiaryOfDreams() {
        // Whenever you cast an instant or sorcery spell, put a page counter on Diary of Dreams.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new PutCountersOnSelfEffect(CounterType.PAGE))));

        // {5}, {T}: Draw a card. This ability costs {1} less to activate for each page counter on Diary of Dreams.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new ReduceActivationCostPerCounterEffect(CounterType.PAGE, 1), new DrawCardEffect(1)),
                "{5}, {T}: Draw a card. This ability costs {1} less to activate for each page counter on Diary of Dreams."
        ));
    }
}
