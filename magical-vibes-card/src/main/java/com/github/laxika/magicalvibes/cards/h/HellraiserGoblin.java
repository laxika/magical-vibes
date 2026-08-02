package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "GTC", collectorNumber = "95")
public class HellraiserGoblin extends Card {

    public HellraiserGoblin() {
        // Creatures you control have haste and attack each combat if able — including this creature.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new MustAttackEffect(GrantScope.ALL_OWN_CREATURES));
    }
}
