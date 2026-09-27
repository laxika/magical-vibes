package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect;

@CardRegistration(set = "40K", collectorNumber = "153")
public class CanoptekWraith extends Card {

    public CanoptekWraith() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{3}", new SacrificeSelfThenEffect(
                        new SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect()),
                        "Pay {3} and sacrifice Canoptek Wraith?"));
    }
}
