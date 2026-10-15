package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAttachedCreatureEffect;

@CardRegistration(set = "CHK", collectorNumber = "251")
public class GeneralsKabuto extends Card {

    public GeneralsKabuto() {
        // Equipped creature has shroud.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.EQUIPPED_CREATURE));

        // Prevent all combat damage that would be dealt to equipped creature.
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToAttachedCreatureEffect());

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
