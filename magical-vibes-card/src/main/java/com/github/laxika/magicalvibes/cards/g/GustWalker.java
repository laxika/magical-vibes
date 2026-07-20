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

@CardRegistration(set = "AKH", collectorNumber = "17")
public class GustWalker extends Card {

    public GustWalker() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +1/+1 and gains
        // flying until end of turn." Modeled as an optional attack trigger (matching Glory-Bound
        // Initiate). Choosing to exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(1, 1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Gust Walker as it attacks? (It gets +1/+1 and gains flying until end of turn.)"
        ));
    }
}
