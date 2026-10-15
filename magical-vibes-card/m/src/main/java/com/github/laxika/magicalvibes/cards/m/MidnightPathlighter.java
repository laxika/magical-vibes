package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "AFC", collectorNumber = "52")
public class MidnightPathlighter extends Card {

    public MidnightPathlighter() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CanBeBlockedOnlyByFilterEffect(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        "legendary creatures"),
                GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new VentureIntoDungeonEffect(), false, true));
    }
}
