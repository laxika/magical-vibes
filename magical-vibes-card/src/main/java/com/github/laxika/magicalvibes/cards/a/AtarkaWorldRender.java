package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "FRF", collectorNumber = "149")
public class AtarkaWorldRender extends Card {

    public AtarkaWorldRender() {
        // Whenever a Dragon you control attacks, it gains double strike until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)));
    }
}
