package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "57")
public class ToilsOfNightAndDay extends Card {

    public ToilsOfNightAndDay() {
        // You may tap or untap target permanent, then you may tap or untap another target permanent.
        // Two separate target groups, each with its own "may" — the prompts must differ so the two
        // MayEffects stay distinct records and keep their own effect→group binding.
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap the first target permanent?"
        ));
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap the other target permanent?"
        ));
    }
}
