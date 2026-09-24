package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EntwineManaCost;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "67")
@CardRegistration(set = "MH2", collectorNumber = "36")
public class UnboundedPotential extends Card {

    public UnboundedPotential() {
        addEffect(EffectSlot.SPELL, new EntwineManaCost("{3}{W}"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each of up to two target creatures",
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        TargetFilters.creature(), null, 0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption("Proliferate", new ProliferateEffect())
        ), false, 1, 2, true));
    }
}
