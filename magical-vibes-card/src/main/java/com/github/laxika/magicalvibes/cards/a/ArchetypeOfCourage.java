package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "BNG", collectorNumber = "4")
public class ArchetypeOfCourage extends Card {

    public ArchetypeOfCourage() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OPPONENT_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.FIRST_STRIKE), GrantScope.OPPONENT_CREATURES));
    }
}
