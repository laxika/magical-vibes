package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "69")
public class StingingLionfish extends Card {

    public StingingLionfish() {
        // Whenever you cast your first spell during each opponent's turn, you may tap or untap
        // target nonland permanent.
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(null,
                                List.of(new TapOrUntapTargetPermanentEffect()),
                                null, null, null, true, false, null, 1),
                        "Tap or untap target nonland permanent?"));
    }
}
