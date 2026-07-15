#!/usr/bin/env python3
"""Effect-coupling audit (refactor step 1).

Scans magical-vibes-engine and magical-vibes-ai for `instanceof <EffectType>`
checks that dispatch on CONCRETE effect types OUTSIDE the sanctioned registry
zones. Produces the coupling matrix report and a machine-readable ratchet
baseline.

============================================================================
COUNTING RULES (duplicated in the Java ratchet test EffectDispatchRatchetTest,
step 2 of the refactor). If you change a rule here you MUST change it there in
lockstep, or the baseline and the test will disagree.
============================================================================

Effect type name set:
  Every top-level type declared under
  magical-vibes-domain/.../model/effect/ (file name minus .java).

A `instanceof X` is a VIOLATION when ALL hold:
  - X is in the effect type name set,
  - X is NOT a structural wrapper (see STRUCTURAL_WRAPPERS),
  - X is NOT a capability/marker INTERFACE (the effect file declares
    `interface`, not `record`/`class`/`enum`),
  - the consuming file is NOT under service/effect/** (normalfx, staticfx,
    the sanctioned handler/registry zone),
  - the consuming file is NOT under service/validate/** (sanctioned validator
    zone).

Matching is word-boundary and also catches the pattern form
`instanceof X var`. Qualified references (`instanceof pkg.X`) are matched on
the final identifier segment.

No arguments; run from anywhere as `python scripts/effect-coupling-audit.py`.
Deterministic and idempotent.
"""

import re
from collections import defaultdict
from pathlib import Path

# --------------------------------------------------------------------------
# Paths
# --------------------------------------------------------------------------
REPO_ROOT = Path(__file__).resolve().parent.parent
EFFECT_PKG = (REPO_ROOT / "magical-vibes-domain" / "src" / "main" / "java"
              / "com" / "github" / "laxika" / "magicalvibes" / "model" / "effect")
ENGINE_SRC = REPO_ROOT / "magical-vibes-engine" / "src" / "main" / "java"
AI_SRC = REPO_ROOT / "magical-vibes-ai" / "src" / "main" / "java"
VALIDATE_DIR = (ENGINE_SRC / "com" / "github" / "laxika" / "magicalvibes"
                / "service" / "validate")
TARGET_DIR = (ENGINE_SRC / "com" / "github" / "laxika" / "magicalvibes"
              / "service" / "target")

OUT_DIR = REPO_ROOT / "refactor-docs"
MATRIX_MD = OUT_DIR / "EFFECT_COUPLING_MATRIX.md"
BASELINE_TXT = OUT_DIR / "effect-dispatch-baseline.txt"

# Structural wrapper types any code may legitimately unwrap (mirrors how
# EffectResolutionService.java treats them). Exempt from violation counting.
# TargetSpec / TargetCategory live in the effect package (so CardEffect.targetSpec()
# can reference them without a cross-package import) but are targeting DESCRIPTORS,
# not dispatchable effects — `instanceof TargetSpec` is never effect-type dispatch, so
# they are exempt here (kept in lockstep with EffectDispatchRatchetTest).
STRUCTURAL_WRAPPERS = {
    "ConditionalEffect",
    "ConditionalReplacementEffect",
    "MayEffect",
    "MayPayManaEffect",
    "MayPayTapPermanentsEffect",
    "ChooseOneEffect",
    "CostEffect",
    "CardEffect",
    "TargetSpec",
    "TargetCategory",
}

INSTANCEOF_RE = re.compile(r"\binstanceof\s+([A-Za-z_][\w.]*)")


# --------------------------------------------------------------------------
# Effect type universe + interface classification
# --------------------------------------------------------------------------
def load_effect_types():
    """Return (all_type_names, interface_names) from the effect package."""
    all_types = set()
    interfaces = set()
    kind_re = re.compile(r"\b(interface|record|enum|class)\s+([A-Za-z_]\w*)")
    for path in sorted(EFFECT_PKG.glob("*.java")):
        name = path.stem
        all_types.add(name)
        text = path.read_text(encoding="utf-8", errors="replace")
        # Classify by the FIRST top-level type keyword that names this file's
        # public type.
        kind = None
        for m in kind_re.finditer(text):
            if m.group(2) == name:
                kind = m.group(1)
                break
        if kind is None:
            # Fallback: first keyword found at all.
            m = kind_re.search(text)
            kind = m.group(1) if m else "record"
        if kind == "interface":
            interfaces.add(name)
    return all_types, interfaces


def eligible_violation_types(all_types, interfaces):
    """Effect types whose `instanceof` counts as a violation."""
    return all_types - STRUCTURAL_WRAPPERS - interfaces


# --------------------------------------------------------------------------
# Consumer scan
# --------------------------------------------------------------------------
def rel(path):
    return path.relative_to(REPO_ROOT).as_posix()


def is_exempt_zone(rel_path):
    return "/service/effect/" in rel_path or "/service/validate/" in rel_path


def module_of(rel_path):
    if rel_path.startswith("magical-vibes-engine/"):
        return "engine"
    if rel_path.startswith("magical-vibes-ai/"):
        return "ai"
    return "other"


def package_label(rel_path):
    """Human-friendly package bucket for the summary tables."""
    marker = "com/github/laxika/magicalvibes/"
    idx = rel_path.find(marker)
    if idx < 0:
        return "(unknown)"
    tail = rel_path[idx + len(marker):]
    parts = tail.split("/")
    dir_parts = parts[:-1]  # drop file name
    if not dir_parts:
        return "(root)"
    # Engine: service/<sub>; AI: ai or ai/<sub>.
    if dir_parts[0] == "service":
        return "service/" + (dir_parts[1] if len(dir_parts) > 1 else "(root)")
    if dir_parts[0] == "ai":
        return "ai" if len(dir_parts) == 1 else "ai/" + dir_parts[1]
    return "/".join(dir_parts)


def scan_sources(eligible, all_types, interfaces):
    """Scan engine + ai sources.

    Returns:
      violations: {rel_path: {type: count}} for non-exempt files/types.
      exempt_zone_hits: count of effect-instanceof inside service/effect|validate.
      structural_hits: {type: count} of wrapper/interface instanceof (any file).
    """
    violations = defaultdict(lambda: defaultdict(int))
    exempt_zone_hits = 0
    structural_hits = defaultdict(int)

    wrapper_and_iface = STRUCTURAL_WRAPPERS | interfaces

    for src_root in (ENGINE_SRC, AI_SRC):
        for path in sorted(src_root.rglob("*.java")):
            rel_path = rel(path)
            text = path.read_text(encoding="utf-8", errors="replace")
            exempt_file = is_exempt_zone(rel_path)
            for m in INSTANCEOF_RE.finditer(text):
                token = m.group(1).split(".")[-1]
                if token not in all_types:
                    continue  # not an effect type at all
                if token in wrapper_and_iface:
                    structural_hits[token] += 1
                    continue
                # token is an eligible concrete effect type
                if exempt_file:
                    exempt_zone_hits += 1
                    continue
                violations[rel_path][token] += 1

    return violations, exempt_zone_hits, structural_hits


# --------------------------------------------------------------------------
# Validator coverage gap
# --------------------------------------------------------------------------
def load_validated_types():
    """Effect types with an @ValidatesTarget validator."""
    validated = set()
    ann_re = re.compile(r"@ValidatesTarget\(\s*([A-Za-z_]\w*)\.class")
    for path in sorted(VALIDATE_DIR.glob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        for m in ann_re.finditer(text):
            validated.add(m.group(1))
    return validated


def targetspec_body(text):
    """Return the body of the effect's targetSpec() override, or None if it has none.

    Brace-depth counting from the method's opening brace so nested switch/lambda braces
    (per-scope / per-recipient specs) are handled correctly.
    """
    m = re.search(r"TargetSpec\s+targetSpec\s*\(\s*\)\s*\{", text)
    if not m:
        return None
    depth = 0
    start = m.end()
    for j in range(m.end() - 1, len(text)):
        c = text[j]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return text[start:j]
    return text[start:]


def load_targeted_types(all_types):
    """Effect types that carry a target.

    Primary signal: the effect overrides targetSpec() to a NON-NONE spec — the target-carrying
    role, per model/effect/CardEffect.java. This is the successor to the pre-step-10 detection
    (a canTarget*() override returning true); the eleven legacy targeting methods were deleted in
    TargetSpec migration step 10, so an effect now declares a target purely through its
    targetSpec() category. A targetSpec() body counts as non-NONE iff it uses a benign(/harmful(
    factory (both take a non-NONE category) or names a TargetCategory value other than NONE.
    Secondary signal: the type is instanceof-checked in the targeting services
    (TargetLegalityService / ValidTargetService).
    """
    targeted = set()
    non_none_cat_re = re.compile(r"\bTargetCategory\.(\w+)")
    factory_re = re.compile(r"\b(?:benign|harmful)\s*\(")
    for path in sorted(EFFECT_PKG.glob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        body = targetspec_body(text)
        if body is None:
            continue
        if factory_re.search(body) or any(
                m.group(1) != "NONE" for m in non_none_cat_re.finditer(body)):
            targeted.add(path.stem)

    for fname in ("TargetLegalityService.java", "ValidTargetService.java"):
        path = TARGET_DIR / fname
        if path.exists():
            text = path.read_text(encoding="utf-8", errors="replace")
            for m in INSTANCEOF_RE.finditer(text):
                token = m.group(1).split(".")[-1]
                if token in all_types:
                    targeted.add(token)
    return targeted


# --------------------------------------------------------------------------
# Report writers
# --------------------------------------------------------------------------
def write_baseline(violations):
    lines = []
    for rel_path in sorted(violations):
        total = sum(violations[rel_path].values())
        if total > 0:
            lines.append(f"{rel_path}={total}")
    BASELINE_TXT.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_matrix(violations, exempt_zone_hits, structural_hits,
                 interfaces, validated, targeted, gap):
    # Aggregate.
    file_totals = {rp: sum(c.values()) for rp, c in violations.items()}
    total_violations = sum(file_totals.values())

    per_module = defaultdict(int)
    per_package = defaultdict(int)
    for rp, tot in file_totals.items():
        per_module[module_of(rp)] += tot
        per_package[(module_of(rp), package_label(rp))] += tot

    # effect type -> consumers
    type_to_files = defaultdict(set)
    for rp, counts in violations.items():
        for t in counts:
            type_to_files[t].add(rp)

    out = []
    out.append("# Effect coupling matrix")
    out.append("")
    out.append("Generated by `scripts/effect-coupling-audit.py` (refactor step 1). "
               "Counts `instanceof <ConcreteEffectType>` dispatch that lives OUTSIDE "
               "the sanctioned registry zones (`service/effect/**`, "
               "`service/validate/**`) and is not a structural wrapper or capability "
               "interface. Regenerate with `python scripts/effect-coupling-audit.py`.")
    out.append("")
    out.append(f"- **Total violations:** {total_violations}")
    out.append(f"- **Offending files:** {len(file_totals)}")
    out.append(f"- **Distinct effect types dispatched on:** {len(type_to_files)}")
    out.append("")

    # (i) consumer file -> violation count
    out.append("## (i) Consumer files by violation count")
    out.append("")
    out.append("| Rank | File | Module | Package | Violations |")
    out.append("|------|------|--------|---------|-----------|")
    ranked = sorted(file_totals.items(), key=lambda kv: (-kv[1], kv[0]))
    for i, (rp, tot) in enumerate(ranked, 1):
        out.append(f"| {i} | `{rp}` | {module_of(rp)} | "
                   f"{package_label(rp)} | {tot} |")
    out.append("")

    # (ii) effect type -> consumer files
    out.append("## (ii) Effect type -> consumer files (outside exempt zones)")
    out.append("")
    out.append("Only effect types with at least one out-of-zone `instanceof` are "
               "listed.")
    out.append("")
    out.append("| Effect type | Refs | Consumer files |")
    out.append("|-------------|------|----------------|")
    for t in sorted(type_to_files, key=lambda x: (-sum(
            violations[f][x] for f in type_to_files[x]), x)):
        files = sorted(type_to_files[t])
        refs = sum(violations[f][t] for f in files)
        file_list = ", ".join(f"`{Path(f).name}`" for f in files)
        out.append(f"| `{t}` | {refs} | {file_list} |")
    out.append("")

    # (iii) summary per module / per package
    out.append("## (iii) Summary")
    out.append("")
    out.append("### Per module")
    out.append("")
    out.append("| Module | Violations |")
    out.append("|--------|-----------|")
    for mod in sorted(per_module):
        out.append(f"| {mod} | {per_module[mod]} |")
    out.append(f"| **total** | **{total_violations}** |")
    out.append("")
    out.append("### Per package")
    out.append("")
    out.append("| Module | Package | Violations |")
    out.append("|--------|---------|-----------|")
    for (mod, pkg) in sorted(per_package):
        out.append(f"| {mod} | {pkg} | {per_package[(mod, pkg)]} |")
    out.append("")

    # (iv) exempt / structural
    out.append("## (iv) Exempt / structural counts (NOT violations)")
    out.append("")
    out.append(f"- **Effect-instanceof inside exempt zones** "
               f"(`service/effect/**`, `service/validate/**`): {exempt_zone_hits}")
    struct_total = sum(structural_hits.values())
    out.append(f"- **Structural-wrapper / capability-interface instanceof** "
               f"(any file): {struct_total}")
    out.append("")
    if structural_hits:
        out.append("| Wrapper / interface | Instanceof count |")
        out.append("|---------------------|------------------|")
        for t in sorted(structural_hits, key=lambda x: (-structural_hits[x], x)):
            kind = "interface" if t in interfaces else "wrapper"
            out.append(f"| `{t}` ({kind}) | {structural_hits[t]} |")
        out.append("")

    # Validator coverage gap
    out.append("## Validator coverage gap")
    out.append("")
    out.append("Targeted effect types (they carry a target: override `targetSpec()` "
               "to a non-NONE spec per `model/effect/CardEffect.java`, "
               "or are instanceof-checked in the targeting services) that have "
               "**no** `@ValidatesTarget` validator under `service/validate/`. "
               "On the single-`targetId` validation path "
               "(`TargetLegalityService.checkSpellTargeting`) these effects get "
               "NO type checking — this is the class of bug that let the AI cast "
               "Fireball at a Plains (July 2026, `DealDividedDamageEffect`). "
               "This list feeds refactor step 3.")
    out.append("")
    out.append(f"- **Targeted effect types:** {len(targeted)}")
    out.append(f"- **With a validator:** {len(targeted & validated)}")
    out.append(f"- **Coverage gap (targeted, no validator):** {len(gap)}")
    out.append("")
    if gap:
        out.append("| Effect type | Dispatched outside zones? |")
        out.append("|-------------|---------------------------|")
        for t in sorted(gap):
            flagged = "yes" if t in type_to_files else ""
            out.append(f"| `{t}` | {flagged} |")
        out.append("")

    MATRIX_MD.write_text("\n".join(out) + "\n", encoding="utf-8")


# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------
def main():
    OUT_DIR.mkdir(exist_ok=True)
    all_types, interfaces = load_effect_types()
    eligible = eligible_violation_types(all_types, interfaces)

    violations, exempt_zone_hits, structural_hits = scan_sources(
        eligible, all_types, interfaces)

    validated = load_validated_types()
    targeted = load_targeted_types(all_types)
    gap = targeted - validated - STRUCTURAL_WRAPPERS - interfaces

    write_baseline(violations)
    write_matrix(violations, exempt_zone_hits, structural_hits,
                 interfaces, validated, targeted, gap)

    total = sum(sum(c.values()) for c in violations.values())
    print(f"effect types: {len(all_types)} "
          f"(interfaces {len(interfaces)}, wrappers {len(STRUCTURAL_WRAPPERS)})")
    print(f"total violations: {total} across {len(violations)} files")
    print(f"exempt-zone hits: {exempt_zone_hits}; "
          f"structural/interface hits: {sum(structural_hits.values())}")
    print(f"validator gap: {len(gap)} targeted effect types without a validator")
    print(f"wrote {rel(MATRIX_MD)} and {rel(BASELINE_TXT)}")


if __name__ == "__main__":
    main()
