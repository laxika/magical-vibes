package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "AKH", collectorNumber = "16")
public class GloryBoundInitiate extends Card {

    public GloryBoundInitiate() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +1/+3 and gains
        // lifelink until end of turn." Modeled as an optional attack trigger (matching Devoted
        // Crop-Mate). Choosing to exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 3),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Glory-Bound Initiate as it attacks? (It gets +1/+3 and gains lifelink until end of turn.)"
        ));
    }
}
