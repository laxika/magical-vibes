package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "60")
@CardRegistration(set = "IMA", collectorNumber = "52")
public class DistortionStrike extends Card {

    public DistortionStrike() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 0))
                .addSpellEffect(new MakeCreatureUnblockableEffect());
    }
}
