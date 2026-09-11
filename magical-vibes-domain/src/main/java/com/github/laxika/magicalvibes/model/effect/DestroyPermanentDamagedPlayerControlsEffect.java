package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import java.util.List;
import java.util.UUID;

/**
 * "Destroy target permanent that player controls" where "that player" is the damaged player.
 * Damage trigger that binds the damaged player's identity before choosing a permanent target.
 * Only fires when the source dealt at least {@code minimumDamage} damage to the player.
 * {@link #forDamagedPlayer(UUID)} creates the targeted effect used on the stack.
 *
 * @param predicate      filter restricting valid targets (e.g. {@code PermanentIsLandPredicate} for lands)
 * @param minimumDamage  the trigger only fires if this much damage or more was dealt (Deus of Calamity: 6)
 */
public record DestroyPermanentDamagedPlayerControlsEffect(PermanentPredicate predicate, int minimumDamage)
        implements CardEffect {

    /** Binds the damaged player before the trigger's target is chosen. */
    public DestroyTargetPermanentEffect forDamagedPlayer(UUID playerId) {
        PermanentPredicate controller = new PermanentControlledByPlayerPredicate(playerId);
        return new DestroyTargetPermanentEffect(predicate == null ? controller
                : new PermanentAllOfPredicate(List.of(predicate, controller)));
    }
}
