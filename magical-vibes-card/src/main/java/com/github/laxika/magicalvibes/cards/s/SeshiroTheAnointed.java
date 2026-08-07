package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "241")
public class SeshiroTheAnointed extends Card {

    public SeshiroTheAnointed() {
        // Other Snake creatures you control get +2/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SNAKE)));

        // Whenever a Snake you control deals combat damage to a player, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SNAKE),
                        new MayEffect(new DrawCardEffect(1), "draw a card")));
    }
}
