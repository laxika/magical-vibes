package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Judges {@link CantAttackOrBlockAloneEffect} ("can't attack or block alone", "can't attack unless at least two other
 * creatures attack", "unless a creature with greater power also blocks") for the attack and block declarations and the
 * AI that builds them (CR 508.1c, 509.1b). A creature's restrictions are its active static abilities, so a face-down
 * creature or one that lost its abilities has none of its own, and the restrictions the Auras and Equipment attached to
 * it impose ({@code AttachedPermanentRestrictionEffect}: a bestowed Sightless Brawler's "enchanted creature can't attack
 * alone"), which stay the attachment's abilities (CR 613.1f); a companion filter is judged with the restricted creature
 * as its source, as the declaration is made.
 */
@Component
@RequiredArgsConstructor
public class CombatCompanionRestrictions {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /** The restrictions on {@code creature} for an attack ({@code attacking}) or a block. */
    public List<CantAttackOrBlockAloneEffect> restrictions(GameData gameData, Permanent creature, boolean attacking) {
        return Stream.concat(gameQueryService.getActiveStaticEffects(gameData, creature).stream()
                                .filter(CantAttackOrBlockAloneEffect.class::isInstance)
                                .map(CantAttackOrBlockAloneEffect.class::cast),
                        gameQueryService.attachedPermanentRestrictions(gameData, creature,
                                CantAttackOrBlockAloneEffect.class).stream())
                .filter(restriction -> attacking ? restriction.restrictsAttacking() : restriction.restrictsBlocking())
                .toList();
    }

    /** Whether {@code creature} has any such restriction, which a lone attacker or blocker always disobeys. */
    public boolean isRestricted(GameData gameData, Permanent creature, boolean attacking) {
        return !restrictions(gameData, creature, attacking).isEmpty();
    }

    /**
     * The first restriction on {@code creature} that the other declared attackers or blockers {@code declared} (the
     * creature itself may be among them) don't meet, or {@code null} when every one is met.
     */
    public CantAttackOrBlockAloneEffect firstUnmet(GameData gameData, Permanent creature, boolean attacking,
                                                   Collection<Permanent> declared) {
        for (CantAttackOrBlockAloneEffect restriction : restrictions(gameData, creature, attacking)) {
            long companions = declared.stream()
                    .filter(other -> !other.getId().equals(creature.getId()))
                    .filter(other -> isCompanion(gameData, creature, restriction, other))
                    .count();
            if (companions < restriction.otherCount()) {
                return restriction;
            }
        }
        return null;
    }

    /** Whether {@code other} counts toward {@code restriction} on {@code creature}. */
    public boolean isCompanion(GameData gameData, Permanent creature, CantAttackOrBlockAloneEffect restriction,
                               Permanent other) {
        return restriction.companion() == null
                || predicateEvaluationService.matchesPermanentPredicate(other, restriction.companion(),
                FilterContext.of(gameData)
                        .withSourceCardId(creature.getCard().getId())
                        .withSourcePermanentId(creature.getId())
                        .withSourceControllerId(gameQueryService.findPermanentController(gameData, creature.getId())));
    }
}
