package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "92")
public class DarkImpostor extends Card {

    public DarkImpostor() {
        // {4}{B}{B}: Exile target creature and put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(
                        new ExileTargetPermanentAndTrackWithSourceEffect(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{4}{B}{B}: Exile target creature and put a +1/+1 counter on this creature.",
                TargetFilters.creature()
        ));

        // Dark Impostor has all activated abilities of all creature cards exiled with it.
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfExiledCardsEffect());
    }
}
