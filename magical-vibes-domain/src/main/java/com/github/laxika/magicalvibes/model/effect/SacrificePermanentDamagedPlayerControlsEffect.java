package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import java.util.List;

/**
 * "Choose target permanent that player controls. The player sacrifices that permanent." where "that player" is the
 * player a damage trigger was about (Ashling the Extinguisher). The target is restricted by
 * {@link PermanentControlledByTriggeringPlayerPredicate}, judged against the choice's triggering player as the ability
 * goes on the stack (CR 603.3d) and against the stack entry's as it resolves; the target's controller, that player,
 * sacrifices it (indestructible and regeneration don't apply).
 *
 * @param predicate filter restricting valid choices (e.g. {@code PermanentIsCreaturePredicate}), or {@code null}
 */
public record SacrificePermanentDamagedPlayerControlsEffect(PermanentPredicate predicate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        PermanentPredicate controller = new PermanentControlledByTriggeringPlayerPredicate();
        return TargetSpec.harmful(TargetPredicates.permanents(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller))));
    }
}
