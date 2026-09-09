package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "152")
public class JadedSellSword extends Card {

    public JadedSellSword() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new TreasureManaSpentToCast(),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.HASTE), GrantScope.SELF)));
    }
}
