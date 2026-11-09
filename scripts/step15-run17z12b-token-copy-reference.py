#!/usr/bin/env python3
"""Step 15, run 17z12b: `CreateTokenCopyOfTargetPermanentEffect` names what it copies.

The enter collectors' class-keyed handlers baked the entering permanent (or the entering tokens) into the entry's
`targetId` for this effect, so "a token that's a copy of that creature" never chose a target. The effect now says it
(`copied`: `TARGET`, `TRIGGERING_PERMANENT` "that creature" / "one of them", `EACH_TRIGGERING_PERMANENT` "for each of
them") and reads the entry's triggering permanents; this script adds the component, keeps a 27-argument constructor and
carries the component through the `with...` copies.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
P = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/CreateTokenCopyOfTargetPermanentEffect.java"
s = P.read_text()

s = s.replace("""/**
 * Creates a token that's a copy of the permanent referenced by the stack entry's targetId.
 * Used for "create a token that's a copy of that artifact/creature" triggered abilities
 * where the permanent to copy is determined at trigger time (e.g. Mirrorworks).
 * The token copies all copiable characteristics per CR 707.2.
""", """/**
 * Creates a token that's a copy of a permanent: the permanent referenced by the stack entry's targetId, or with
 * {@link #ofTriggeringPermanent()} / {@link #ofEachTriggeringPermanent()} the permanent (or permanents) the trigger event
 * was about, read from the entry's triggering permanents ("a token that's a copy of that creature", "for each of them"),
 * which the ability doesn't target. A triggering permanent that has left the battlefield is copied as it last existed
 * there (CR 608.2h). The token copies all copiable characteristics per CR 707.2.
""", 1)

s = s.replace("""        boolean hasteUntilEndOfTurn,
        AnimatePermanentsEffect animateIfNotCreature
) implements CardEffect {
    public CreateTokenCopyOfTargetPermanentEffect {
""", """        boolean hasteUntilEndOfTurn,
        AnimatePermanentsEffect animateIfNotCreature,
        Copied copied
) implements CardEffect {

    /** What the token copies. */
    public enum Copied {
        /** The permanent the stack entry targets (or references by its targetId). */
        TARGET,
        /**
         * The permanent the trigger event was about ("that creature"); with several ("one or more … enter"), one of them
         * chosen as the ability resolves ("a copy of one of them").
         */
        TRIGGERING_PERMANENT,
        /** Each permanent the trigger event was about ("for each of them, create a token that's a copy of it"). */
        EACH_TRIGGERING_PERMANENT
    }

    public CreateTokenCopyOfTargetPermanentEffect {
        if (copied == null) {
            copied = Copied.TARGET;
        }
""", 1)

# A 27-argument constructor for the existing full-arity callers.
s = s.replace("""    /** Backward-compatible constructor for copies without a post-entry animation. */""",
"""    /** Backward-compatible constructor for copies of the entry's target. */
    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Integer powerOverride,
            Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters,
            boolean grantHaste,
            boolean exileAtEndStep,
            boolean sacrificeAtEndStep,
            boolean tappedAndAttacking,
            boolean trackWithSource,
            boolean createForTargetController,
            CardColor colorOverride,
            Set<Keyword> additionalKeywords,
            boolean sacrificeAtNextUpkeep,
            List<Ability> additionalAbilities,
            List<CardSubtype> creatureSubtypeOverride,
            boolean tapped,
            boolean removeLegendary,
            DynamicAmount amount,
            boolean exileAtEndOfCombat,
            Set<CardSupertype> additionalSupertypes,
            boolean chooseAttackTarget,
            List<CardColor> additionalColors,
            String nameOverride,
            boolean replaceCardTypes,
            boolean hasteUntilEndOfTurn,
            AnimatePermanentsEffect animateIfNotCreature) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking, trackWithSource,
                createForTargetController, colorOverride, additionalKeywords, sacrificeAtNextUpkeep,
                additionalAbilities, creatureSubtypeOverride, tapped, removeLegendary, amount,
                exileAtEndOfCombat, additionalSupertypes, chooseAttackTarget, additionalColors, nameOverride,
                replaceCardTypes, hasteUntilEndOfTurn, animateIfNotCreature, Copied.TARGET);
    }

    /** Backward-compatible constructor for copies without a post-entry animation. */""", 1)

# The with... copies carry the component.
n = 0
for tail in ("nameOverride, true, hasteUntilEndOfTurn, animateIfNotCreature);",
             "nameOverride, replaceCardTypes, true, animateIfNotCreature);",
             "nameOverride, replaceCardTypes, hasteUntilEndOfTurn, animation);"):
    assert tail in s, tail
    s = s.replace(tail, tail[:-2] + ", copied);", 1)
    n += 1

s = s.replace("""    /**
     * Returns this copy effect with a permanent animation applied""", """    /**
     * This copy effect, copying the permanent the trigger event was about instead of a target ("create a token that's a
     * copy of that creature"); with several ("whenever one or more … enter"), one of them chosen as it resolves.
     */
    public CreateTokenCopyOfTargetPermanentEffect ofTriggeringPermanent() {
        return withCopied(Copied.TRIGGERING_PERMANENT);
    }

    /** This copy effect, copying each permanent the trigger event was about ("for each of them, … a copy of it"). */
    public CreateTokenCopyOfTargetPermanentEffect ofEachTriggeringPermanent() {
        return withCopied(Copied.EACH_TRIGGERING_PERMANENT);
    }

    private CreateTokenCopyOfTargetPermanentEffect withCopied(Copied newCopied) {
        return new CreateTokenCopyOfTargetPermanentEffect(additionalSubtypes, additionalTypes, powerOverride,
                toughnessOverride, initialCounters, grantHaste, exileAtEndStep, sacrificeAtEndStep,
                tappedAndAttacking, trackWithSource, createForTargetController, colorOverride, additionalKeywords,
                sacrificeAtNextUpkeep, additionalAbilities, creatureSubtypeOverride, tapped, removeLegendary,
                amount, exileAtEndOfCombat, additionalSupertypes, chooseAttackTarget, additionalColors,
                nameOverride, replaceCardTypes, hasteUntilEndOfTurn, animateIfNotCreature, newCopied);
    }

    /**
     * Returns this copy effect with a permanent animation applied""", 1)

old = """    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }"""
assert old in s
s = s.replace(old, """    @Override
    public TargetSpec targetSpec() {
        return copied == Copied.TARGET ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }""", 1)
P.write_text(s)
print("with-copies updated:", n)
