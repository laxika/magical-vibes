package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SNC", collectorNumber = "180")
public class CrewCaptain extends Card {

    public CrewCaptain() {
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceEnteredThisTurn(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
