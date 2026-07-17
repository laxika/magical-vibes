package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Static effect that grants a graveyard-activated ability to every creature card in its controller's
 * graveyard (e.g. Sedris, the Traitor King: "Each creature card in your graveyard has unearth {2}{B}.").
 *
 * <p>Passively scanned from the {@code STATIC} slot via the {@link GraveyardAbilityGrantingEffect}
 * capability — there is no resolver. The engine's graveyard ability paths (activation and view
 * building) append {@link #ability()} to the effective graveyard abilities of creature cards owned by
 * the effect's controller while the source permanent is on the battlefield. The granted ability is
 * self-referential (returns the activating card), so a single {@link ActivatedAbility} instance works
 * for every creature card.</p>
 */
public record GrantGraveyardAbilityToCreatureCardsEffect(ActivatedAbility ability)
        implements GraveyardAbilityGrantingEffect {

    @Override
    public ActivatedAbility grantedGraveyardAbility() {
        return ability;
    }
}
