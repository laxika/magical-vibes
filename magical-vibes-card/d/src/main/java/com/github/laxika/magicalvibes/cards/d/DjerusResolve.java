package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "AKH", collectorNumber = "11")
@CardRegistration(set = "AKR", collectorNumber = "15")
public class DjerusResolve extends Card {

    public DjerusResolve() {
        // Untap target creature. Prevent all damage that would be dealt to it this turn.
        target(TargetFilters.creature())
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(PreventDamageEffect.allToTargetCreatures());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
