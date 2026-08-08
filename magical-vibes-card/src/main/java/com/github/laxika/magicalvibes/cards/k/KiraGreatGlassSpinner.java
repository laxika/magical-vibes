package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

@CardRegistration(set = "BOK", collectorNumber = "40")
public class KiraGreatGlassSpinner extends Card {

    public KiraGreatGlassSpinner() {
        // Flying is auto-loaded from Scryfall.

        // Creatures you control have "Whenever this creature becomes the target of a spell or ability
        // for the first time each turn, counter that spell or ability." ALL_OWN_CREATURES includes Kira
        // itself. The "first time each turn" gating and targeting the triggering object are handled in
        // TriggerCollectionService for any counterspelling effect in this slot (as for Glyph Keeper).
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new CounterSpellEffect(),
                GrantScope.ALL_OWN_CREATURES));
    }
}
