package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "12")
public class HiddenStrings extends Card {

    public HiddenStrings() {
        target(TargetFilters.permanent());
        target(TargetFilters.permanent());

        // Unbound so the single tap-or-untap effect reads both target groups; "another" keeps the
        // two targets distinct by the default cross-group uniqueness rule.
        addEffect(EffectSlot.SPELL, new TapOrUntapTargetPermanentEffect());
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
