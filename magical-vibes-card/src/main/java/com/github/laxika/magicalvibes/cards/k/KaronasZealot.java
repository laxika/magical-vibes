package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromSourceCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "18")
public class KaronasZealot extends Card {

    public KaronasZealot() {
        addMorph("{3}{W}{W}");
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new RedirectAllDamageFromSourceCreatureToTargetCreatureEffect());
    }
}
