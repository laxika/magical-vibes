package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Necromancy-style ETB: "When this enchantment enters, if it's on the battlefield, it becomes an
 * Aura with 'enchant creature put onto the battlefield with [this].' Put target creature card from
 * a graveyard onto the battlefield under your control and attach this enchantment to it."
 *
 * <p>Place in {@code ON_ENTER_BATTLEFIELD}. Targets any graveyard
 * ({@link GraveyardSearchScope#ALL_GRAVEYARDS});
 * the shared ETB graveyard-target flow chooses the card as the trigger goes on the stack. Resolution
 * re-checks that the source is still on the battlefield (intervening-if), turns it into an Aura,
 * reanimates the targeted creature under the controller, and attaches the source to it.</p>
 */
public record BecomeAuraReanimateFromGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
