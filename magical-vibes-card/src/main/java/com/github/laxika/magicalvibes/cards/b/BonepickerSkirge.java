package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "86")
public class BonepickerSkirge extends Card {

    public BonepickerSkirge() {
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new OpponentPoisoned(3),
                        new GrantKeywordEffect(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.SELF)));
    }
}
