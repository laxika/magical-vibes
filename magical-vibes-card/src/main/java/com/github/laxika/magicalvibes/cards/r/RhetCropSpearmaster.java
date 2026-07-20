package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "26")
public class RhetCropSpearmaster extends Card {

    public RhetCropSpearmaster() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +1/+0 and gains
        // first strike until end of turn." Modeled as an optional attack trigger (matching Gust
        // Walker). Choosing to exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Rhet-Crop Spearmaster as it attacks? (It gets +1/+0 and gains first strike until end of turn.)"
        ));
    }
}
