package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "BNG", collectorNumber = "116")
public class ArchetypeOfEndurance extends Card {

    public ArchetypeOfEndurance() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.OPPONENT_CREATURES));
        addStaticEffect(new GrantEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.HEXPROOF), GrantScope.OPPONENT_CREATURES));
    }
}
