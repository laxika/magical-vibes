package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M12", collectorNumber = "219")
public class SwiftfootBoots extends Card {

    public SwiftfootBoots() {
        // Equipped creature has hexproof and haste
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
