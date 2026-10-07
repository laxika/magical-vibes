package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect;

@CardRegistration(set = "40K", collectorNumber = "153")
public class CanoptekWraith extends Card {

    public CanoptekWraith() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        // "Pay {3} and sacrifice it" can't be paid once it has left the battlefield
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(new SourceIsOnBattlefield(),
                new MayPayManaEffect("{3}", new SacrificeSelfThenEffect(
                        new SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect()),
                        "Pay {3} and sacrifice Canoptek Wraith?")));
    }
}
