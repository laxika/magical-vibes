package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RNA", collectorNumber = "145")
public class TerritorialBoar extends Card {

    public TerritorialBoar() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(4, new BoostSelfEffect(1, 1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(4,
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));
    }
}
