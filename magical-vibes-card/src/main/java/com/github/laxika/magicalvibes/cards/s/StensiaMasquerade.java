package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "INR", collectorNumber = "172")
public class StensiaMasquerade extends Card {

    public StensiaMasquerade() {
        // Attacking creatures you control have first strike.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES, new PermanentIsAttackingPredicate()));

        // Whenever a Vampire you control deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));

        // Madness {2}{R}
        addCastingOption(new MadnessCast("{2}{R}"));
    }
}
