package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "104")
public class ElderMastery extends Card {

    public ElderMastery() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // Enchanted creature gets +3/+3 and has flying.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));

        // Whenever enchanted creature deals damage to a player, that player discards two cards.
        // (The damaged player is supplied as the effect's target by CombatDamageService.)
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER, false));
    }
}
