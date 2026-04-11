package com.github.laxika.magicalvibes.model.effect;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the invariant that every {@link CardEffect} implementation whose class name begins with
 * {@code Target} declares what it can target by overriding one of the {@code canTarget*} default
 * methods. The trigger-target collection code in {@code TriggeredAbilityQueueService} and
 * {@code StepTriggerService} uses those overrides to decide which UUIDs are valid when a triggered
 * ability needs a target; an effect that silently inherits the {@code false} defaults will never be
 * offered any valid target (see Black Cat's death trigger fix).
 *
 * <p>A small allowlist captures effects that legitimately target nothing themselves:
 * <ul>
 *   <li><b>Piggyback effects</b> — effects that reuse an already-chosen target from their stack
 *       entry (e.g. companion effects riding on a counterspell).</li>
 *   <li><b>Pre-resolved effects</b> — effects that store the resolved target UUID in the record
 *       itself (or are pushed with {@code nonTargeting=true}) and so never enter the shared
 *       target-collection pipeline.</li>
 * </ul>
 * When adding a new effect to the allowlist, please document which bucket it falls into above
 * its entry below.
 */
class CardEffectTargetingConsistencyTest {

    /** Canonical names of canTarget* overrides we consider as "declares its own targeting". */
    private static final Set<String> CAN_TARGET_METHOD_NAMES = Set.of(
            "canTargetPlayer",
            "canTargetPermanent",
            "canTargetSpell",
            "canTargetGraveyard",
            "canTargetAnyGraveyard",
            "canTargetExile"
    );

    /**
     * Effects whose class name starts with {@code Target} but which intentionally do not override
     * any canTarget* method. Every entry must be one of:
     * <ul>
     *   <li>a piggyback effect that rides on a target already chosen by a sibling effect, or</li>
     *   <li>a pre-resolved effect whose target UUID is baked into the record / pushed with
     *       {@code nonTargeting=true}.</li>
     * </ul>
     */
    private static final Set<String> ALLOWLIST = Set.of(
            // Piggyback: rides on the shared target of the stack entry (the targeted planeswalker's
            // controller / targeted player). Does not add its own targeting.
            "TargetPlayerCreaturesCantBlockThisTurnEffect",
            // Pre-resolved: the losing player's UUID is stored in the record constructor
            // (used by emblem/delayed effects that already know the player).
            "TargetPlayerLosesGameEffect",
            // Piggyback: rides on a counterspell's existing target (e.g. Frightful Delusion,
            // Psychic Barrier).
            "TargetSpellControllerDiscardsEffect",
            "TargetSpellControllerLosesLifeEffect"
    );

    @Test
    @DisplayName("Every Target*Effect overrides a canTarget* method or is documented as piggyback/pre-resolved")
    void everyTargetEffectDeclaresItsTargeting() {
        List<String> violations = new ArrayList<>();

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .acceptPackages("com.github.laxika.magicalvibes.model.effect")
                .scan()) {

            for (ClassInfo info : scan.getClassesImplementing(CardEffect.class.getName())) {
                String simpleName = info.getSimpleName();
                if (!simpleName.startsWith("Target")) continue;
                if (info.isAbstract() || info.isInterface()) continue;

                Class<?> effectClass;
                try {
                    effectClass = Class.forName(info.getName());
                } catch (ClassNotFoundException e) {
                    throw new AssertionError("ClassGraph found " + info.getName()
                            + " but it failed to load", e);
                }

                boolean declaresTargeting = false;
                for (Method m : effectClass.getDeclaredMethods()) {
                    if (CAN_TARGET_METHOD_NAMES.contains(m.getName())
                            && m.getParameterCount() == 0
                            && m.getReturnType() == boolean.class) {
                        declaresTargeting = true;
                        break;
                    }
                }

                if (!declaresTargeting && !ALLOWLIST.contains(simpleName)) {
                    violations.add(simpleName);
                }
            }
        }

        assertThat(violations)
                .as("Effects named Target*Effect must override a canTarget* method or be added to "
                        + "ALLOWLIST in CardEffectTargetingConsistencyTest with a comment explaining "
                        + "why (piggyback vs pre-resolved). Without an override, trigger-target "
                        + "collection in TriggeredAbilityQueueService / StepTriggerService will "
                        + "never offer any valid target for the effect.")
                .isEmpty();
    }

    @Test
    @DisplayName("Allowlist entries still exist in the codebase")
    void allowlistEntriesExist() {
        List<String> stale = new ArrayList<>();
        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("com.github.laxika.magicalvibes.model.effect")
                .scan()) {

            Set<String> foundNames = new java.util.HashSet<>();
            for (ClassInfo info : scan.getClassesImplementing(CardEffect.class.getName())) {
                foundNames.add(info.getSimpleName());
            }
            for (String entry : ALLOWLIST) {
                if (!foundNames.contains(entry)) {
                    stale.add(entry);
                }
            }
        }
        assertThat(stale)
                .as("ALLOWLIST in CardEffectTargetingConsistencyTest references classes that no "
                        + "longer exist — clean them up.")
                .isEmpty();
    }
}
