#!/usr/bin/env python3
"""Step 15 run 10: the effects stop implementing CombatDamageAmountAwareEffect.

Every combat damage trigger's stack entry now carries the damage dealt as its event value, set by
the collector, so no effect has to say that it reads it. For each record that implements the
interface this script:

- drops `CombatDamageAmountAwareEffect` from its `implements` list (a record left with no
  interface implements `CardEffect`);
- deletes its `combatDamageAmount()` override;
- deletes its `snapshotCombatDamage(int)` override for the effects whose amount is read as they
  resolve (`RESOLUTION_SNAPSHOTS`); the three graveyard-target effects are edited by hand;
- removes imports the file no longer uses.

Rerunnable: a converted file no longer names the interface and is skipped.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EFFECTS = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"

RESOLUTION_SNAPSHOTS = {
    "SeekCardsToHandEffect",
    "MayCastAnySpellFromHandWithoutPayingManaCostEffect",
    "DealDamageToTargetPermanentEffect",
    "DealDamageToTargetCreatureOrPlaneswalkerEffect",
}
HAND_EDITED = {
    "ReturnCardFromGraveyardEffect",
    "ExileTargetCardFromGraveyardAndMayCastCopyEffect",
    "ExileUpToCombatDamageCardsFromGraveyardEffect",
}


def remove_method(text: str, signature_regex: str) -> str:
    """Removes an `@Override`-annotated method (and the blank line before it) by brace matching."""
    match = re.search(r"\n(\n)?    @Override\n    " + signature_regex + r"[^{]*\{", text)
    if not match:
        return text
    depth = 0
    index = match.end() - 1
    while True:
        char = text[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                break
        index += 1
    end = index + 1
    if text[end] == "\n":
        end += 1
    keep_newline = "\n"
    return text[:match.start()] + keep_newline + text[end:]


def drop_interface(text: str) -> str:
    text = re.sub(r",\s*CombatDamageAmountAwareEffect\b", "", text)
    text = re.sub(r"\bCombatDamageAmountAwareEffect,\s*", "", text)
    text = re.sub(r"implements\s+CombatDamageAmountAwareEffect\b", "implements CardEffect", text)
    return text


def drop_unused_imports(text: str) -> str:
    lines = text.split("\n")
    body = "\n".join(line for line in lines if not line.startswith("import "))
    kept = []
    for line in lines:
        imported = re.match(r"import (?:static )?[\w.]+\.(\w+);$", line)
        if imported and not re.search(rf"\b{imported.group(1)}\b", body):
            continue
        kept.append(line)
    text = "\n".join(kept)
    header_end = re.search(r"^(/\*\*|@|public )", text, re.MULTILINE).start()
    return re.sub(r"\n\n\n+", "\n\n", text[:header_end]) + text[header_end:]


def convert(path: Path) -> bool:
    name = path.stem
    if name == "CombatDamageAmountAwareEffect" or name in HAND_EDITED:
        return False
    text = path.read_text()
    if not re.search(r"\bCombatDamageAmountAwareEffect\b", text):
        return False
    text = drop_interface(text)
    text = remove_method(text, r"public DynamicAmount combatDamageAmount\(\)")
    if name in RESOLUTION_SNAPSHOTS:
        text = remove_method(text, r"public \w+ snapshotCombatDamage\(int damageDealt\)")
    text = drop_unused_imports(text)
    if "snapshotCombatDamage" in text or "combatDamageAmount" in text:
        raise SystemExit(f"{name}: left a combat damage method behind")
    path.write_text(text)
    return True


def main() -> None:
    changed = [path.stem for path in sorted(EFFECTS.glob("*.java")) if convert(path)]
    print(f"converted {len(changed)} records")
    for name in changed:
        print(f"  {name}")


if __name__ == "__main__":
    sys.exit(main())
