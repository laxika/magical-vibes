package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A player declared attackers (CR 508.1): the event "whenever this creature attacks", "whenever you attack" and the
 * other attack abilities trigger on (CR 508.3a). One declaration is one occurrence (CR 603.2c).
 *
 * @param attackingPlayerId the player who declared the attackers
 * @param attackerIds       the creatures declared as attackers, in declaration order
 * @param attackTargets     what each attacker attacks: a player, planeswalker or battle
 * @param declared          each attacker as it was declared, for "those creatures" and "their total power"
 * @param enlisted          for each attacker that enlisted a creature as it attacked (CR 702.154a), that creature
 */
public record AttackEvent(UUID attackingPlayerId, List<UUID> attackerIds, Map<UUID, UUID> attackTargets,
                          List<Permanent> declared, Map<UUID, UUID> enlisted) implements RulesEvent {

    public AttackEvent {
        Objects.requireNonNull(attackingPlayerId, "attackingPlayerId");
        attackerIds = List.copyOf(attackerIds);
        attackTargets = new LinkedHashMap<>(attackTargets);
        declared = List.copyOf(declared);
        enlisted = new LinkedHashMap<>(enlisted);
    }

    @Override
    public AttackEvent deepCopy() {
        return new AttackEvent(attackingPlayerId, attackerIds, attackTargets,
                declared.stream().map(Permanent::new).toList(), enlisted);
    }
}
