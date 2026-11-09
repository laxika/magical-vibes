package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEC", collectorNumber = "109")
@CardRegistration(set = "C17", collectorNumber = "28")
public class ShiftingShadow extends Card {

    public ShiftingShadow() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has "At the beginning of your upkeep, destroy this creature. Reveal cards from
                // the top of your library until you reveal a creature card. Put that card onto the battlefield and
                // attach Shifting Shadow to it, then put all other cards revealed this way on the bottom of your
                // library in a random order."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect()),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
