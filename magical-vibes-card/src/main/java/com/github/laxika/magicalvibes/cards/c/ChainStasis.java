package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HML", collectorNumber = "23")
public class ChainStasis extends Card {

    public ChainStasis() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TapOrUntapTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new MayPayManaEffect(
                        "{2}{U}",
                        new CopyThisSpellForTargetControllerEffect(),
                        "Pay {2}{U} to copy Chain Stasis?",
                        MayPayPayer.TARGET_PERMANENT_CONTROLLER
                ));
    }
}
