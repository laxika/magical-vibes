package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "134")
public class CallOfTheFullMoon extends Card {

    public CallOfTheFullMoon() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +3/+2 and has trample.
                .addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, sacrifice this Aura.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new SacrificeSelfEffect()));
    }
}
