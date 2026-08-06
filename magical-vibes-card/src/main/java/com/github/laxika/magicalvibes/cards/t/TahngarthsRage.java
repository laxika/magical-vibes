package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Enchanted creature gets +3/+0 as long as it's attacking. Otherwise, it gets -2/-1.
 *
 * <p>The switch reads the <em>enchanted creature's</em> combat state, not the Aura's, hence
 * {@code amountsFromAttachedCreature}.
 */
@CardRegistration(set = "TMP", collectorNumber = "209")
public class TahngarthsRage extends Card {

    public TahngarthsRage() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new IfSourceAttacking(new Fixed(3), new Fixed(-2)),
                new IfSourceAttacking(new Fixed(0), new Fixed(-1)),
                GrantScope.ENCHANTED_CREATURE,
                true));
    }
}
