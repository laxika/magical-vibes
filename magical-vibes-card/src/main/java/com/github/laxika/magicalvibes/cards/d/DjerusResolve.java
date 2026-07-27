package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "AKH", collectorNumber = "11")
public class DjerusResolve extends Card {

    public DjerusResolve() {
        // Untap target creature. Prevent all damage that would be dealt to it this turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allToTargetCreatures());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
