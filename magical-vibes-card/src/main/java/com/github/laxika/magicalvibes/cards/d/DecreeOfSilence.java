package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "32")
public class DecreeOfSilence extends Card {

    public DecreeOfSilence() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new CounterSpellEffect(),
                        new PutCountersOnSelfEffect(CounterType.DEPLETION),
                        ConditionalEffect.unless(
                                new SourceCounterThreshold(3, CounterType.DEPLETION),
                                new SacrificeSelfEffect())
                )
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{U}",
                List.of(new MayEffect(new CounterSpellEffect(), "Counter target spell?"), new DrawCardEffect(1)),
                "Cycling {4}{U}{U} ({4}{U}{U}, Discard this card: Draw a card.)",
                null,
                null,
                null,
                null,
                List.of(),
                0,
                1));
    }
}
