package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "BNG", collectorNumber = "32")
@CardRegistration(set = "DSC", collectorNumber = "111")
@CardRegistration(set = "C18", collectorNumber = "81")
public class ArchetypeOfImagination extends Card {

    public ArchetypeOfImagination() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.OPPONENT_CREATURES));
        addStaticEffect(new GrantEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.FLYING), GrantScope.OPPONENT_CREATURES));
    }
}
