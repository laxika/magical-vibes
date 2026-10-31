package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "BNG", collectorNumber = "58")
public class ArchetypeOfFinality extends Card {

    public ArchetypeOfFinality() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new RemoveKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OPPONENT_CREATURES));
        addStaticEffect(new GrantEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.DEATHTOUCH), GrantScope.OPPONENT_CREATURES));
    }
}
