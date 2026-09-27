package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOC", collectorNumber = "221")
public class EchoStorm extends Card {

    public EchoStorm() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForEachCommanderCastEffect(false));
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect());
    }
}
