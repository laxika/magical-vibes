package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;
import java.util.UUID;

/**
 * Effect on a permanent (any controller) or a floating effect: every creature matching {@code matcher}
 * must attack each combat if able, regardless of who controls it. Global analogue of the
 * self-only {@link MustAttackEffect}, read in
 * {@code CombatAttackService.getMustAttackRequirementCount}. Per CR 508.1d the controller
 * is not required to pay any attack costs even when this effect is present.
 * Used by Goblin Assault (Goblin creatures), and as a floating effect by Taunt ("creatures that player controls
 * attack you if able", {@code requiredAttackTargetId} the player or permanent they must attack) and Sizzling
 * Soloist.
 *
 * @param matcher              the creatures that must attack
 * @param requiredAttackTargetId the player or planeswalker they must attack, or {@code null} for any legal target
 */
public record MatchingCreaturesMustAttackEffect(PermanentPredicate matcher, UUID requiredAttackTargetId)
        implements CombatAttackRequirementEffect {

    public MatchingCreaturesMustAttackEffect(PermanentPredicate matcher) {
        this(matcher, null);
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return matcher;
    }

    @Override
    public Set<UUID> requiredAttackTargetIds(GameData gameData, Permanent sourcePermanent) {
        return requiredAttackTargetId == null ? Set.of() : Set.of(requiredAttackTargetId);
    }
}
