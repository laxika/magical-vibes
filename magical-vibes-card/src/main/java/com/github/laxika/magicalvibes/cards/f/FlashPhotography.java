package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIC", collectorNumber = "463")
public class FlashPhotography extends Card {

    public FlashPhotography() {
        setFlashCastTargetPredicate(new PermanentControlledBySourceControllerPredicate());
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{4}{U}{U}"));
    }
}
