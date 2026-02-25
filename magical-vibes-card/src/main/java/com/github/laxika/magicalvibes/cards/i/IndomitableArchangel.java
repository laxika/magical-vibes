package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "SOM", collectorNumber = "11")
public class IndomitableArchangel extends Card {

    public IndomitableArchangel() {
        // Metalcraft — Artifacts you control have shroud as long as you control three or more artifacts.
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(
                new GrantKeywordEffect(Keyword.SHROUD, GrantScope.OWN_PERMANENTS, new PermanentIsArtifactPredicate())));
    }
}
