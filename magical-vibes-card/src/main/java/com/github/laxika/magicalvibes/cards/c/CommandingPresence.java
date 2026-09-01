package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "7")
public class CommandingPresence extends Card {

    public CommandingPresence() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(2, 2, Set.of(Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.whiteSoldier(1));
    }
}
