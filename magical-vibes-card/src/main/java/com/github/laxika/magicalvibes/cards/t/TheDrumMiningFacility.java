package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WHO", collectorNumber = "581")
public class TheDrumMiningFacility extends Card {

    public TheDrumMiningFacility() {
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new EndTurnEffect());
    }
}
