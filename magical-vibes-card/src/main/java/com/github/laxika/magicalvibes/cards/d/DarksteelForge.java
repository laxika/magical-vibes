package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "M14", collectorNumber = "206")
public class DarksteelForge extends Card {

    public DarksteelForge() {
        // Artifacts you control have indestructible. OWN_PERMANENTS excludes the source from
        // static bonus computation, so SELF (with the same artifact filter) covers the Forge itself.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF,
                new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS,
                new PermanentIsArtifactPredicate()));
    }
}
