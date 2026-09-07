package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "26")
public class MythRealized extends Card {

    public MythRealized() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new PutCountersOnSelfEffect(CounterType.LORE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.LORE)),
                "{2}{W}: Put a lore counter on this enchantment."
        ));

        CountersOnSource loreCounters = new CountersOnSource(CounterType.LORE);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(AnimatePermanentsEffect.withDynamicPowerToughness(
                        loreCounters, loreCounters,
                        List.of(CardSubtype.MONK, CardSubtype.AVATAR), Set.of(), Set.of()
                )),
                "{W}: Until end of turn, this enchantment becomes a Monk Avatar creature in addition to its other types and gains \"This creature's power and toughness are each equal to the number of lore counters on it.\""
        ));
    }
}
