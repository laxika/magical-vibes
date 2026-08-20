package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "DRK", collectorNumber = "78")
public class HiddenPath extends Card {

    public HiddenPath() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
    }
}
