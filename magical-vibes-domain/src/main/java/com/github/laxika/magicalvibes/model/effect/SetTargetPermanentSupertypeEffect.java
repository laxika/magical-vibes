package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * One-shot type-changing effect (CR 613.1d layer 4) that gives the targeted permanent a supertype
 * or takes one away for as long as it remains on the battlefield. Arcum's Weathervane uses both
 * directions: "target snow land is no longer snow" is {@code (SNOW, false)} and "target nonsnow
 * basic land becomes snow" is {@code (SNOW, true)}.
 *
 * <p>The change is recorded on the permanent itself ({@code persistentGrantedSupertypes} /
 * {@code persistentRemovedSupertypes}) and read back through
 * {@code GameQueryService.hasEffectiveSupertype}. The two directions are mutually exclusive, so a
 * later activation simply overrides an earlier one.
 *
 * <p>The spec is left as an unnarrowed benign permanent target — which permanents are legal
 * ("snow land", "nonsnow basic land") differs per ability and is expressed with the
 * {@code ActivatedAbility}'s {@code TargetFilter}.
 *
 * @param supertype the supertype to grant or remove
 * @param gained    {@code true} to grant the supertype, {@code false} to remove it
 */
public record SetTargetPermanentSupertypeEffect(CardSupertype supertype, boolean gained) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
