package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "100")
public class SubtleStrike extends Card {

    public SubtleStrike() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        CardEffect weaken = new BoostTargetCreatureEffect(-1, -1);
        CardEffect strengthen = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -1/-1 until end of turn",
                        weaken,
                        creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature",
                        strengthen,
                        creature)
        )));
    }
}
