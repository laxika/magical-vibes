package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HML", collectorNumber = "91")
public class MammothHarness extends Card {

    public MammothHarness() {
        // Enchant creature
        target(TargetFilters.creature());

        // Enchanted creature loses flying.
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));

        // Whenever enchanted creature blocks or becomes blocked by a creature,
        // the other creature gains first strike until end of turn.
        // GrantKeywordEffect with TARGET scope references the combat opponent, so the block
        // pipeline auto-binds it as a non-targeting target.
        addTrigger(Triggers.blocks(), new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
