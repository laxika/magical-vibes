package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M21", collectorNumber = "188")
public class HeroicIntervention extends Card {

    public HeroicIntervention() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS, GrantDuration.END_OF_TURN));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS, GrantDuration.END_OF_TURN));
    }
}
