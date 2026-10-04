package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import java.util.List;
import java.util.UUID;

/**
 * "Choose target permanent that player controls. The player sacrifices that permanent." where
 * "that player" is the damaged player. Mandatory combat damage trigger (sacrifice variant of
 * {@link DestroyPermanentDamagedPlayerControlsEffect}). Only fires when the source dealt at least
 * {@code minimumDamage} damage to the player and that player controls a matching permanent.
 * The source's controller chooses the permanent; the damaged player sacrifices it (ignores
 * indestructible/regeneration).
 * {@link #forDamagedPlayer(UUID)} binds that player before the trigger's target is selected.
 *
 * @param predicate      filter restricting valid choices (e.g. {@code PermanentIsCreaturePredicate})
 * @param minimumDamage  the trigger only fires if this much damage or more was dealt
 * @param damagedPlayerId the bound damaged player for a targeted stack ability, or null before binding
 */
public record SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate, int minimumDamage,
                                                            UUID damagedPlayerId)
        implements DamagedPlayerControlsTargetEffect {

    public SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate, int minimumDamage) {
        this(predicate, minimumDamage, null);
    }

    @Override
    public CardEffect forDamagedPlayer(UUID playerId) {
        return new SacrificePermanentDamagedPlayerControlsEffect(predicate, minimumDamage, playerId);
    }

    @Override
    public TargetSpec targetSpec() {
        if (damagedPlayerId == null) {
            return TargetSpec.NONE;
        }
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(damagedPlayerId);
        return TargetSpec.harmful(TargetPredicates.permanents(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller))));
    }
}
