package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "39")
public class CorruptedKey extends Card {

    public CorruptedKey() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsTapped(),
                new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.DEATHTOUCH), GrantScope.OWN_CREATURES)));
    }
}
