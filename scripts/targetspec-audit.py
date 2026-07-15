#!/usr/bin/env python3
"""TargetSpec invariant checker (refactor step 11 close-out).

The TargetSpec migration is COMPLETE: the eleven legacy per-effect targeting
methods on CardEffect were deleted (step 10) and every effect now exposes its
targeting through the single declarative `TargetSpec targetSpec()`. This script
is no longer the step-1 audit that generated a shrinking ratchet baseline — that
baseline (`refactor-docs/targetspec-baseline.txt`) and worklist matrix
(`refactor-docs/TARGETSPEC_MATRIX.md`) were consumed by the migration and
deleted. What remains is the PERMANENT invariant checker: it verifies the two
cheap, static invariants that keep the new mechanism from eroding.

No arguments; run from anywhere as `python scripts/targetspec-audit.py`.
Deterministic. Prints a report and exits non-zero if either invariant is broken.

============================================================================
THE TWO INVARIANTS
============================================================================
1. NO REINTRODUCTION OF A LEGACY TARGETING METHOD. No file under
   model/effect/ may DECLARE a method named canTargetPlayer / canTargetPermanent
   / canTargetSpell / canTargetGraveyard / canTargetAnyGraveyard /
   targetsControllersGraveyardOnly / canTargetExile / targetPredicate /
   isSelfTargeting / requiredPlayerTargetCount / isDamageOrDestruction. Matched
   as `public <boolean|int|PermanentPredicate> <name>() {` — a method with a
   body. Record COMPONENTS named `canTargetSpell` / `targetPredicate` (on the
   two duals ChangeColorTextEffect / PutCounterOnTargetPermanentEffect) are NOT
   method declarations with a body and do not match.

2. EVERY @ValidatesTarget EFFECT DECLARES A NON-NONE targetSpec(). A
   hand-written @ValidatesTarget validator is now ONLY an escape hatch for
   non-structural rules (opponent-relation, controller/owner compare,
   chosen-source, null-target tolerance); such an effect must STILL declare its
   structural spec so the declarative interpreter offers and type-checks it. A
   targetSpec() body counts as non-NONE iff it uses a benign( / harmful( factory
   or names a TargetCategory value other than NONE (a conditional body with at
   least one non-NONE branch counts). The two equip/attach validators
   (StaticBoostEffect, AttachedBoostEffect) target outside the single-target
   pipeline and carry no targetSpec() category by design — they are EXEMPT.

============================================================================
LOCKSTEP CONTRACT with TargetSpecRatchetTest
============================================================================
Both invariants are DUPLICATED in the Java guard
`magical-vibes-application/.../architecture/TargetSpecRatchetTest.java` (same
LEGACY_METHODS set, same `public <boolean|int|PermanentPredicate> <name>()`
override regex, same brace-matched targetSpec() non-NONE detection, same two
EQUIP_ATTACH_VALIDATED_EFFECTS exemptions). If you change how an invariant is
computed here you MUST change it there in lockstep, or the script and the test
will disagree.
"""

import re
import sys
from pathlib import Path

# --------------------------------------------------------------------------
# Paths
# --------------------------------------------------------------------------
REPO_ROOT = Path(__file__).resolve().parent.parent
EFFECT_PKG = (REPO_ROOT / "magical-vibes-domain" / "src" / "main" / "java"
              / "com" / "github" / "laxika" / "magicalvibes" / "model" / "effect")
VALIDATE_DIR = (REPO_ROOT / "magical-vibes-engine" / "src" / "main" / "java"
                / "com" / "github" / "laxika" / "magicalvibes" / "service" / "validate")

# The 11 legacy targeting methods the migration deleted (isPowerToughnessDefining is KEPT).
LEGACY_METHODS = [
    "canTargetPlayer", "canTargetPermanent", "canTargetSpell",
    "canTargetGraveyard", "canTargetAnyGraveyard", "targetsControllersGraveyardOnly",
    "canTargetExile", "targetPredicate", "isSelfTargeting",
    "requiredPlayerTargetCount", "isDamageOrDestruction",
]

# @ValidatesTarget effects that legitimately expose no targetSpec() category:
# they target through the equip/attach mechanism, not the single-target pipeline.
EQUIP_ATTACH_VALIDATED_EFFECTS = {"StaticBoostEffect", "AttachedBoostEffect"}

VALIDATES_TARGET_RE = re.compile(r"@ValidatesTarget\(\s*([A-Za-z_]\w*)\.class")
TARGET_SPEC_METHOD_RE = re.compile(r"TargetSpec\s+targetSpec\s*\(\s*\)\s*\{")
SPEC_FACTORY_RE = re.compile(r"\b(?:benign|harmful)\s*\(")
SPEC_CATEGORY_RE = re.compile(r"\bTargetCategory\.(\w+)")


def rel(path):
    return path.relative_to(REPO_ROOT).as_posix()


def method_override_re(name):
    # public boolean/int/PermanentPredicate <name>( ) {  — a method with a body.
    return re.compile(
        r"public\s+(?:boolean|int|PermanentPredicate)\s+" + re.escape(name)
        + r"\s*\(\s*\)\s*\{")


def overridden_legacy_methods(text):
    """Distinct legacy targeting methods the file declares as an override."""
    return [m for m in LEGACY_METHODS if method_override_re(m).search(text)]


def brace_matched_body(text, open_brace_idx):
    """Body between matching braces starting at open_brace_idx (excludes outer braces)."""
    depth = 0
    for i in range(open_brace_idx, len(text)):
        c = text[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return text[open_brace_idx + 1:i]
    return text[open_brace_idx + 1:]


def declares_non_none_targetspec(text):
    """True iff the file overrides targetSpec() with a body that can return a non-NONE spec."""
    m = TARGET_SPEC_METHOD_RE.search(text)
    if not m:
        return False
    body = brace_matched_body(text, m.end() - 1)
    if SPEC_FACTORY_RE.search(body):
        return True
    return any(mm.group(1) != "NONE" for mm in SPEC_CATEGORY_RE.finditer(body))


# --------------------------------------------------------------------------
# Invariant checks
# --------------------------------------------------------------------------
def check_invariant_1():
    """No effect file declares any deleted legacy targeting method."""
    problems = []
    for path in sorted(EFFECT_PKG.glob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        overridden = overridden_legacy_methods(text)
        if overridden:
            problems.append((rel(path), overridden))
    return problems


def check_invariant_2():
    """Every @ValidatesTarget effect (bar equip/attach) declares a non-NONE targetSpec()."""
    validated = set()
    for path in sorted(VALIDATE_DIR.glob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        for m in VALIDATES_TARGET_RE.finditer(text):
            validated.add(m.group(1))

    problems = []
    for effect in sorted(validated):
        if effect in EQUIP_ATTACH_VALIDATED_EFFECTS:
            continue
        effect_file = EFFECT_PKG / (effect + ".java")
        if not effect_file.exists():
            problems.append((effect, "no such effect file"))
            continue
        if not declares_non_none_targetspec(effect_file.read_text(encoding="utf-8", errors="replace")):
            problems.append((effect, "targetSpec() is absent or NONE"))
    return validated, problems


# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------
def main():
    inv1 = check_invariant_1()
    validated, inv2 = check_invariant_2()

    print("TargetSpec invariant check (migration complete — permanent guard)")
    print("-" * 64)

    if inv1:
        print(f"INVARIANT 1 FAILED: {len(inv1)} effect file(s) declare a deleted legacy method:")
        for path, methods in inv1:
            print(f"  - {path}: {', '.join(methods)}")
    else:
        print("INVARIANT 1 OK: no effect file declares any of the 11 deleted legacy targeting methods.")

    print(f"@ValidatesTarget effects: {len(validated)} "
          f"(exempt equip/attach: {', '.join(sorted(EQUIP_ATTACH_VALIDATED_EFFECTS))})")
    if inv2:
        print(f"INVARIANT 2 FAILED: {len(inv2)} validated effect(s) lack a non-NONE targetSpec():")
        for effect, why in inv2:
            print(f"  - {effect}: {why}")
    else:
        print("INVARIANT 2 OK: every @ValidatesTarget effect (bar equip/attach) declares a non-NONE targetSpec().")

    if inv1 or inv2:
        sys.exit(1)
    print("-" * 64)
    print("Both invariants hold.")


if __name__ == "__main__":
    main()
