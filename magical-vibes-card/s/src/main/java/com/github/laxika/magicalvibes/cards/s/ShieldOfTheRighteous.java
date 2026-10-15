package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "ARB", collectorNumber = "11")
public class ShieldOfTheRighteous extends Card {

    public ShieldOfTheRighteous() {
        // Equipped creature gets +0/+2 and has vigilance.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature blocks a creature, that creature doesn't untap during its
        // controller's next untap step. SkipNextUntapEffect is a CombatOpponentReferencingEffect, so
        // the trigger auto-targets the blocked attacker.
        addEffect(EffectSlot.ON_BLOCK, new SkipNextUntapEffect(TapUntapScope.TARGET));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
