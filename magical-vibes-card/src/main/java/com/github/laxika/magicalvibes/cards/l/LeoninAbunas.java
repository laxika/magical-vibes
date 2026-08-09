package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "8")
public class LeoninAbunas extends Card {

    public LeoninAbunas() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS,
                new PermanentIsArtifactPredicate()));
    }
}
