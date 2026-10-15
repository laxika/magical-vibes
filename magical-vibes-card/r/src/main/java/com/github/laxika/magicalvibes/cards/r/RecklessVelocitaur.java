package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DFT", collectorNumber = "144")
public class RecklessVelocitaur extends Card {

    public RecklessVelocitaur() {
        addEffect(EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
                new BoostSelfEffect(2, 0));
        addEffect(EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
