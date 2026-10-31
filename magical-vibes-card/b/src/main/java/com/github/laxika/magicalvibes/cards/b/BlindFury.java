package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DoubleCombatDamageToCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "MIR", collectorNumber = "158")
public class BlindFury extends Card {

    public BlindFury() {
        addSpellEffect(new RemoveKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_CREATURES));
        addSpellEffect(new DoubleCombatDamageToCreaturesThisTurnEffect());
    }
}
