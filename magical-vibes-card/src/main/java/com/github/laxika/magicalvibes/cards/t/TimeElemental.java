package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "129")
@CardRegistration(set = "4ED", collectorNumber = "108")
public class TimeElemental extends Card {

    public TimeElemental() {
        // When this creature attacks or blocks, at end of combat, sacrifice it and it deals 5 damage to you.
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect(5));
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect(5));

        // {2}{U}{U}, {T}: Return target permanent that isn't enchanted to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}",
                List.of(ReturnToHandEffect.target()),
                "{2}{U}{U}, {T}: Return target permanent that isn't enchanted to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsEnchantedPredicate()),
                        "Target must be a permanent that isn't enchanted")));
    }
}
