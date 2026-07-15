#!/usr/bin/env python3
"""TargetSpec migration audit (refactor step 1).

Scans the domain effect package for every record that overrides at least one of
the LEGACY per-effect targeting methods on CardEffect, and cross-references the
@ValidatesTarget validators (the semantic oracle) under service/validate/.
Produces the human worklist (TARGETSPEC_MATRIX.md) and the machine-readable
ratchet baseline (targetspec-baseline.txt).

No arguments; run from anywhere as `python scripts/targetspec-audit.py`.
Deterministic and idempotent — regenerates deliverables 2 and 3 on every run.

============================================================================
WHAT COUNTS AS A LEGACY TARGETING METHOD
============================================================================
The 11 overridable targeting methods on CardEffect that this migration will
delete (isPowerToughnessDefining is KEPT and is NOT in this set):

  canTargetPlayer, canTargetPermanent, canTargetSpell, canTargetGraveyard,
  canTargetAnyGraveyard, targetsControllersGraveyardOnly, canTargetExile,
  targetPredicate, isSelfTargeting, requiredPlayerTargetCount,
  isDamageOrDestruction

A record is "in scope" (appears in the baseline + matrix) iff it provides an
own override of >=1 of these. The baseline count for a file is the number of
DISTINCT legacy methods it overrides.

============================================================================
LOCKSTEP CONTRACT with TargetSpecRatchetTest
============================================================================
The per-file legacy-override COUNTING RULES below (the LEGACY_METHODS set and
the `method_override_re` regex that matches `public <boolean|int|
PermanentPredicate> <name>()`) are DUPLICATED in the Java ratchet
`magical-vibes-application/.../architecture/TargetSpecRatchetTest.java`, which
parses the `targetspec-baseline.txt` this script writes and fails when any
file's count drifts from its baseline. If you change how a file's count is
computed here you MUST change it there in lockstep, or the freshly regenerated
baseline and the ratchet will disagree and the ratchet becomes noise.

============================================================================
CONDITIONAL OVERRIDE
============================================================================
An override body is CONSTANT when, after stripping comments and whitespace, it
is exactly `return true;` / `return false;` / `return null;` / `return <int>;`.
Anything else (reads a record component, branches on scope, etc.) is
CONDITIONAL and is flagged: the per-instance spec must be computed from the
record's components by the migration step, not hard-coded.

============================================================================
VALIDATOR "BEYOND STRUCTURE" SIGNALS (escape hatch)
============================================================================
A @ValidatesTarget validator whose (helper-inlined) body contains any of the
four non-structural logics below cannot be replaced by a declarative spec and
must be KEPT WHOLE as the permanent escape hatch:

  - opponent-relation  : the body mentions an opponent relation
                         (`opponent`, requireOpponent).
  - controller-compare : compares the target's controller/owner to the source
                         controller (findSourcePermanentController, getController,
                         controllerId, graveyard-owner equality).
  - chosen-source      : inspects a chosen source (chosenSource / ChosenSource).
  - null-target-tol    : tolerates a null targetId by RETURNING (not throwing)
                         (divided damage / ETB-assignment paths).

Structure the spec CAN express (NOT escape-hatch): requireBattlefieldTarget
(PERMANENT), requireCreature (CREATURE), requireTargetPlayer (PLAYER),
hasType(LAND/PLANESWALKER/...) narrowing, playerIds.contains split, graveyard
zone/type checks, matchesCardPredicate against effect.filter() (-> spec
PermanentPredicate), checkProtection (-> harmful flag).
"""

import re
from collections import defaultdict, Counter
from pathlib import Path

# --------------------------------------------------------------------------
# Paths
# --------------------------------------------------------------------------
REPO_ROOT = Path(__file__).resolve().parent.parent
EFFECT_PKG = (REPO_ROOT / "magical-vibes-domain" / "src" / "main" / "java"
              / "com" / "github" / "laxika" / "magicalvibes" / "model" / "effect")
VALIDATE_DIR = (REPO_ROOT / "magical-vibes-engine" / "src" / "main" / "java"
                / "com" / "github" / "laxika" / "magicalvibes" / "service" / "validate")

OUT_DIR = REPO_ROOT / "refactor-docs"
MATRIX_MD = OUT_DIR / "TARGETSPEC_MATRIX.md"
BASELINE_TXT = OUT_DIR / "targetspec-baseline.txt"

# The 11 legacy targeting methods this migration deletes.
LEGACY_METHODS = [
    "canTargetPlayer", "canTargetPermanent", "canTargetSpell",
    "canTargetGraveyard", "canTargetAnyGraveyard", "targetsControllersGraveyardOnly",
    "canTargetExile", "targetPredicate", "isSelfTargeting",
    "requiredPlayerTargetCount", "isDamageOrDestruction",
]
CAN_TARGET_ZONE = {
    "canTargetPlayer", "canTargetPermanent", "canTargetSpell",
    "canTargetGraveyard", "canTargetAnyGraveyard", "canTargetExile",
}

# Structural wrappers delegate targeting to inner effects; they still override
# legacy methods (by delegation) so they stay in the baseline, but they migrate
# by delegating targetSpec(), not by carrying a fixed category.
STRUCTURAL_WRAPPERS = {
    "ConditionalEffect", "ConditionalReplacementEffect", "MayEffect",
    "MayPayManaEffect", "MayPayTapPermanentsEffect", "ChooseOneEffect",
    "CostEffect",
}

# Proposed TargetCategory enum (confirmed / trimmed against the data at runtime).
PROPOSED_CATEGORIES = [
    "NONE", "PLAYER", "PLAYER_OR_PERMANENT", "PERMANENT", "CREATURE", "LAND",
    "CREATURE_OR_PLANESWALKER", "PLAYER_OR_PLANESWALKER", "ANY_TARGET",
    "SPELL_ON_STACK", "GRAVEYARD_CARD", "ANY_GRAVEYARD_CARD", "EXILE_CARD",
]


# --------------------------------------------------------------------------
# Generic Java helpers
# --------------------------------------------------------------------------
def strip_comments(text):
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.DOTALL)
    text = re.sub(r"//[^\n]*", " ", text)
    return text


def extract_body(text, start_brace_idx):
    """Return the source between the matching braces starting at start_brace_idx
    (which must index the opening '{'). Excludes the outer braces."""
    depth = 0
    for i in range(start_brace_idx, len(text)):
        c = text[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return text[start_brace_idx + 1:i]
    return text[start_brace_idx + 1:]


def rel(path):
    return path.relative_to(REPO_ROOT).as_posix()


# --------------------------------------------------------------------------
# Effect-record scan
# --------------------------------------------------------------------------
CONSTANT_BODY_RE = re.compile(r"^return\s+(true|false|null|-?\d+)\s*;$")


def method_override_re(name):
    # public boolean/int/PermanentPredicate <name>( ) {
    return re.compile(
        r"public\s+(?:boolean|int|PermanentPredicate)\s+" + re.escape(name)
        + r"\s*\(\s*\)\s*\{")


def load_type_kind(text, type_name):
    m = re.search(r"\b(interface|record|enum|class)\s+" + re.escape(type_name), text)
    return m.group(1) if m else "record"


def scan_effects():
    """Return list of per-effect dicts for records overriding >=1 legacy method."""
    effects = []
    for path in sorted(EFFECT_PKG.glob("*.java")):
        name = path.stem
        text = path.read_text(encoding="utf-8", errors="replace")
        kind = load_type_kind(text, name)
        overrides = {}          # method -> "constant" | "conditional" | "false"
        const_false = []        # canTarget* overridden to constant false (redundant)
        for method in LEGACY_METHODS:
            m = method_override_re(method).search(text)
            if not m:
                continue
            body = extract_body(text, m.end() - 1)
            norm = re.sub(r"\s+", " ", strip_comments(body)).strip()
            constant = bool(CONSTANT_BODY_RE.match(norm))
            if constant and norm == "return false;":
                overrides[method] = "false"
                if method in CAN_TARGET_ZONE:
                    const_false.append(method)
            else:
                overrides[method] = "constant" if constant else "conditional"
        if not overrides:
            continue
        effects.append({
            "name": name,
            "path": rel(path),
            "kind": kind,
            "overrides": overrides,
            "const_false": const_false,
        })
    return effects


# --------------------------------------------------------------------------
# Validator scan
# --------------------------------------------------------------------------
VALIDATES_RE = re.compile(r"@ValidatesTarget\(\s*([A-Za-z_]\w*)\.class\s*\)")


def scan_validators():
    """Return effect_name -> validator dict, plus ordered list of all validators."""
    by_effect = {}
    all_validators = []          # (file, effect, method) in file order
    for path in sorted(VALIDATE_DIR.glob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        # Index every method: name -> effective body (own body only for now).
        method_bodies = {}
        for mm in re.finditer(r"\b(?:public|private|void)\b[^;{]*?\b(\w+)\s*\([^)]*\)\s*\{", text):
            mname = mm.group(1)
            body = extract_body(text, mm.end() - 1)
            method_bodies[mname] = body
        # Walk @ValidatesTarget annotations and bind each to the method it precedes.
        for am in VALIDATES_RE.finditer(text):
            effect = am.group(1)
            mm = re.compile(r"\b(?:public|void)\b[^;{]*?\b(\w+)\s*\(([^)]*)\)\s*\{").search(
                text, am.end())
            if not mm:
                continue
            method = mm.group(1)
            params = mm.group(2)
            has_effect_param = bool(re.search(r"\b" + re.escape(effect) + r"\s+\w+", params))
            body = method_bodies.get(method, "")
            eff_body = inline_helpers(body, method_bodies)
            info = {
                "file": path.name,
                "method": method,
                "effect": effect,
                "has_effect_param": has_effect_param,
                "body": eff_body,
            }
            info["signals"] = beyond_structure_signals(eff_body)
            info["category_hint"] = validator_category_hint(eff_body)
            by_effect[effect] = info
            all_validators.append((path.name, effect, method))
    return by_effect, all_validators


def inline_helpers(body, method_bodies):
    """Inline one level of private-helper calls so shared validation logic is
    visible when scanning a thin @ValidatesTarget method that delegates."""
    eff = body
    for called in set(re.findall(r"\b(validate\w+)\s*\(", body)):
        if called in method_bodies:
            eff += "\n" + method_bodies[called]
    return eff


def beyond_structure_signals(body):
    sig = []
    stripped = strip_comments(body)
    low = stripped.lower()
    if re.search(r"\bopponent", low) or "requireopponent" in low:
        sig.append("opponent-relation")
    if ("findsourcepermanentcontroller" in low or "getcontroller(" in low
            or "controllerid" in low or "findgraveyardownerbyid" in low):
        sig.append("controller-compare")
    if "chosensource" in low:
        sig.append("chosen-source")
    # null-targetId TOLERANCE: `targetId() == null` guarded by a return, not a throw.
    for m in re.finditer(r"targetid\(\)\s*==\s*null", low):
        tail = low[m.end():m.end() + 140]
        ret = tail.find("return")
        thr = tail.find("throw")
        if ret != -1 and (thr == -1 or ret < thr):
            sig.append("null-target-tol")
            break
    return sig


def validator_category_hint(body):
    """Refine a permanent/player target category from the validator body."""
    s = strip_comments(body)
    creature = "requirecreature" in s.lower() or re.search(r"isCreature\s*\(", s)
    planeswalker = "PLANESWALKER" in s
    land = "CardType.LAND" in s
    player_split = "playerids.contains" in s.lower() or "requiretargetplayer" in s.lower()
    return {
        "creature": bool(creature),
        "planeswalker": bool(planeswalker),
        "land": bool(land),
        "player_split": bool(player_split),
    }


# --------------------------------------------------------------------------
# Bucketing + category assignment
# --------------------------------------------------------------------------
def structural_flags(overrides):
    # A canTarget* override contributes to the target type only when it can be
    # true (constant-true or conditional); a redundant `return false;` does not.
    def on(method):
        return method in overrides and overrides[method] != "false"
    return {
        "player": on("canTargetPlayer"),
        "permanent": on("canTargetPermanent"),
        "spell": on("canTargetSpell"),
        "graveyard": on("canTargetGraveyard"),
        "anyGraveyard": on("canTargetAnyGraveyard"),
        "exile": on("canTargetExile"),
    }


def bucket_of(flags):
    if flags["spell"]:
        return "spell"
    if flags["graveyard"] or flags["anyGraveyard"]:
        return "graveyard+any" if flags["anyGraveyard"] else "graveyard"
    if flags["exile"]:
        return "exile"
    if flags["player"] and flags["permanent"]:
        return "player+permanent"
    if flags["permanent"]:
        return "permanent-only"
    if flags["player"]:
        return "player-only"
    return "metadata-only"


BUCKET_ORDER = [
    "permanent-only", "player+permanent", "player-only", "spell",
    "graveyard", "graveyard+any", "exile", "metadata-only",
]
BUCKET_TITLES = {
    "permanent-only": "Permanent-only (canTargetPermanent)",
    "player+permanent": "Player + permanent (any-target family)",
    "player-only": "Player-only (canTargetPlayer)",
    "spell": "Spell on the stack (canTargetSpell)",
    "graveyard": "Graveyard card (canTargetGraveyard)",
    "graveyard+any": "Graveyard card incl. any graveyard (canTargetAnyGraveyard)",
    "exile": "Exile card (canTargetExile)",
    "metadata-only": "Metadata-only (no canTarget*: self / damage-or-destruction / predicate / player-count)",
}


def assign_category(flags, validator):
    hint = validator["category_hint"] if validator else {
        "creature": False, "planeswalker": False, "land": False, "player_split": False}
    if flags["spell"]:
        return "SPELL_ON_STACK"
    if flags["graveyard"] or flags["anyGraveyard"]:
        return "ANY_GRAVEYARD_CARD" if flags["anyGraveyard"] else "GRAVEYARD_CARD"
    if flags["exile"]:
        return "EXILE_CARD"
    if flags["player"] and flags["permanent"]:
        if hint["creature"] and hint["planeswalker"]:
            return "ANY_TARGET"
        if hint["planeswalker"]:
            return "PLAYER_OR_PLANESWALKER"
        return "PLAYER_OR_PERMANENT"
    if flags["permanent"]:
        if hint["creature"] and hint["planeswalker"]:
            return "CREATURE_OR_PLANESWALKER"
        if hint["creature"]:
            return "CREATURE"
        if hint["land"]:
            return "LAND"
        return "PERMANENT"
    if flags["player"]:
        return "PLAYER"
    return "NONE"


# --------------------------------------------------------------------------
# Writers
# --------------------------------------------------------------------------
def legacy_cell(overrides):
    parts = []
    for m in LEGACY_METHODS:
        if m in overrides:
            suffix = {"conditional": "*", "false": " (=false)"}.get(overrides[m], "")
            parts.append(m + suffix)
    return ", ".join(parts)


def write_baseline(effects):
    lines = []
    for e in sorted(effects, key=lambda x: x["path"]):
        lines.append(f"{e['path']}={len(e['overrides'])}")
    BASELINE_TXT.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_matrix(effects, validators_by_effect, all_validators):
    for e in effects:
        e["flags"] = structural_flags(e["overrides"])
        e["bucket"] = ("wrapper" if e["name"] in STRUCTURAL_WRAPPERS
                       else bucket_of(e["flags"]))
        e["validator"] = validators_by_effect.get(e["name"])
        e["category"] = ("(delegated)" if e["bucket"] == "wrapper"
                         else assign_category(e["flags"], e["validator"]))

    total_overrides = sum(len(e["overrides"]) for e in effects)
    cond_effects = [e for e in effects if any(v == "conditional" for v in e["overrides"].values())]
    with_validator = [e for e in effects if e["validator"]]

    # Category counts (only effects placed in a real category; wrappers excluded).
    cat_counts = Counter()
    for e in effects:
        if e["bucket"] == "wrapper":
            continue
        cat_counts[e["category"]] += 1

    out = []
    out.append("# TargetSpec migration matrix")
    out.append("")
    out.append("Generated by `scripts/targetspec-audit.py` (refactor step 1 — audit). "
               "One row per domain effect **record** that overrides at least one of the "
               "11 legacy targeting methods on `CardEffect`. Regenerate with "
               "`python scripts/targetspec-audit.py`. The migration steps tick the "
               "**Done** checkbox as each record moves to a `targetSpec()` override.")
    out.append("")
    out.append(f"- **Records in scope (override >=1 legacy method):** {len(effects)}")
    out.append(f"- **Total legacy method-overrides (== sum of baseline counts):** {total_overrides}")
    out.append(f"- **Records with >=1 CONDITIONAL override (need per-instance spec):** {len(cond_effects)}")
    out.append(f"- **Records with a @ValidatesTarget validator:** {len(with_validator)}")
    out.append(f"- **@ValidatesTarget methods total:** {len(all_validators)} "
               f"(124 at the step-1 audit; shrinks as structural validators retire during migration)")
    out.append("")
    out.append("`*` after a legacy method name means the override body is CONDITIONAL "
               "(reads record components / branches) — its spec must be computed "
               "per-instance, not hard-coded.")
    out.append("")

    # Legacy method tally
    out.append("## Legacy method override tally")
    out.append("")
    out.append("| Legacy method | Records overriding | Conditional overrides |")
    out.append("|---------------|--------------------|-----------------------|")
    for m in LEGACY_METHODS:
        n = sum(1 for e in effects if m in e["overrides"])
        c = sum(1 for e in effects if e["overrides"].get(m) == "conditional")
        out.append(f"| `{m}` | {n} | {c} |")
    out.append("")

    # TargetCategory proposal
    out.append("## Proposed `TargetCategory` enum (confirmed against the data)")
    out.append("")
    out.append("Coarse category inferred from the record's `canTarget*` booleans, refined "
               "for validated effects by the validator body (`requireCreature` -> CREATURE, "
               "`hasType(LAND)` -> LAND, planeswalker/player splits -> the mixed values). "
               "Fine-grained narrowing (artifact-only, nonland, subtype) is expressed with "
               "the spec's `PermanentPredicate` field, NOT a new enum value. Counts exclude "
               "the delegating structural wrappers.")
    out.append("")
    out.append("| TargetCategory | Effects mapped | Keep? |")
    out.append("|----------------|----------------|-------|")
    for cat in PROPOSED_CATEGORIES:
        n = cat_counts.get(cat, 0)
        keep = "yes" if n > 0 else "**DROP (unused)**"
        out.append(f"| `{cat}` | {n} | {keep} |")
    # Any category the data demanded that wasn't proposed?
    extra = sorted(set(cat_counts) - set(PROPOSED_CATEGORIES))
    for cat in extra:
        out.append(f"| `{cat}` (NEW — data demanded) | {cat_counts[cat]} | yes |")
    out.append("")
    out.append("Reminder: for validated effects the FINAL category is chosen by reading the "
               "kept validator during migration; the value above is the audit's inference. "
               "For UNVALIDATED effects the spec must reproduce the `canTarget*` booleans "
               "EXACTLY (no narrowing) — so PERMANENT/PLAYER/PLAYER_OR_PERMANENT stand as-is "
               "unless the official ruling is checked (BEHAVIOR-PRESERVING-OR-STRICTER).")
    out.append("")

    # Per-bucket worklist tables
    out.append("## Worklist by bucket")
    out.append("")
    buckets = defaultdict(list)
    for e in effects:
        buckets[e["bucket"]].append(e)
    order = BUCKET_ORDER + [b for b in buckets if b not in BUCKET_ORDER and b != "wrapper"]
    if "wrapper" in buckets:
        order = order + ["wrapper"]
    for bucket in order:
        rows = buckets.get(bucket)
        if not rows:
            continue
        title = BUCKET_TITLES.get(
            bucket, "Structural wrappers (delegate targetSpec to inner effects)"
            if bucket == "wrapper" else bucket)
        out.append(f"### {title} — {len(rows)} records")
        out.append("")
        out.append("| Done | Effect record | Legacy methods overridden | Validator (file :: method) | Proposed category |")
        out.append("|:----:|---------------|---------------------------|----------------------------|-------------------|")
        for e in sorted(rows, key=lambda x: x["name"]):
            v = e["validator"]
            vcell = f"`{v['file']}` :: `{v['method']}`" if v else "—"
            out.append(f"| [ ] | `{e['name']}` | {legacy_cell(e['overrides'])} | {vcell} | `{e['category']}` |")
        out.append("")

    # Escape-hatch section
    out.append("## Permanent `@ValidatesTarget` escape hatch")
    out.append("")
    out.append("Effects whose validator contains logic BEYOND target-type structure. Per the "
               "shared invariant KEPT VALIDATORS ARE KEPT WHOLE, these validators stay "
               "unchanged after migration (the spec cannot express opponent relations, "
               "controller/owner comparisons, chosen-source inspection, or null-target "
               "tolerance). The `harmful` flag on their spec is set iff the kept validator "
               "calls `checkProtection`.")
    out.append("")
    esc = []
    for e in effects:
        v = e["validator"]
        if v and v["signals"]:
            esc.append(e)
    out.append(f"- **Escape-hatch effects:** {len(esc)}")
    out.append("")
    out.append("| Effect record | Validator (file :: method) | Non-structural signals | harmful (checkProtection)? |")
    out.append("|---------------|----------------------------|------------------------|----------------------------|")
    for e in sorted(esc, key=lambda x: x["name"]):
        v = e["validator"]
        harmful = "yes" if "checkprotection" in v["body"].lower() else "no"
        out.append(f"| `{e['name']}` | `{v['file']}` :: `{v['method']}` | {', '.join(v['signals'])} | {harmful} |")
    out.append("")

    # Validator index — proves all 124 appear exactly once
    out.append("## Validator index (all @ValidatesTarget methods)")
    out.append("")
    out.append("Every `@ValidatesTarget` method, once each. `In matrix?` is yes when its "
               "effect overrides a legacy method (so it also appears in a bucket table above); "
               "`no` means the effect targets via another mechanism (trigger/ETB/multi-target "
               "slot) and overrides no legacy method — it is tracked here only.")
    out.append("")
    out.append("| # | File | Effect | Method | In a bucket table? | Escape hatch? |")
    out.append("|---|------|--------|--------|:------------------:|:-------------:|")
    in_scope_names = {e["name"] for e in effects}
    for i, (file, effect, method) in enumerate(all_validators, 1):
        in_matrix = "yes" if effect in in_scope_names else "no"
        v = validators_by_effect.get(effect)
        esc_flag = "yes" if (v and v["signals"]) else ""
        out.append(f"| {i} | `{file}` | `{effect}` | `{method}` | {in_matrix} | {esc_flag} |")
    out.append("")

    MATRIX_MD.write_text("\n".join(out) + "\n", encoding="utf-8")
    return {
        "total_overrides": total_overrides,
        "cond": len(cond_effects),
        "with_validator": len(with_validator),
        "cat_counts": cat_counts,
        "escape": len(esc),
        "const_false": [e for e in effects if e["const_false"]],
    }


# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------
def main():
    OUT_DIR.mkdir(exist_ok=True)
    effects = scan_effects()
    validators_by_effect, all_validators = scan_validators()

    write_baseline(effects)
    stats = write_matrix(effects, validators_by_effect, all_validators)

    perm = sum(1 for e in effects if "canTargetPermanent" in e["overrides"])
    print(f"records in scope: {len(effects)} "
          f"(sum of baseline counts = {stats['total_overrides']} legacy overrides)")
    print(f"canTargetPermanent overrides: {perm} (sanity: baseline sum {stats['total_overrides']} >= 146)")
    print(f"conditional-override records: {stats['cond']}")
    print(f"validators total: {len(all_validators)} (124 at step-1 audit); "
          f"records with a validator: {stats['with_validator']}")
    print(f"escape-hatch validators: {stats['escape']}")
    if stats["const_false"]:
        print("WARNING: canTarget* overridden to constant false in: "
              + ", ".join(e["name"] for e in stats["const_false"]))
    print(f"category counts: {dict(sorted(stats['cat_counts'].items()))}")
    print(f"wrote {rel(MATRIX_MD)} and {rel(BASELINE_TXT)}")


if __name__ == "__main__":
    main()
