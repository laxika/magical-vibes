#!/usr/bin/env python3
"""Step 15, run 17z11f7: the agent docs stop describing a staged ETB division and the bound imprinted card.

A divided triggered ability announces its targets and division as it is put on the stack; nothing staged in
`GameData.pendingETBDamageAssignments` is taken over, and tests declare a division with `harness.declareDivision(...)`.
A registered ability's "the exiled card" is the registration's linked exiled card, put on the entry by its collector
(`CopyImprintedCardAndMayCastCopyEffect.boundTo` is gone).

Idempotent; fails if a doc still names a removed form. Usage: step15-run17z11f7-docs.py [repository-root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "EFFECTS_INDEX.md": [
        ("Wrap in `MayEffect` for optional sacrifice. Damage assignments provided before cast via "
         "`pendingETBDamageAssignments`. Does NOT use standard targeting |",
         "Wrap in `MayEffect` for optional sacrifice. As the triggered ability is put on the stack its controller "
         "announces zero or more targets and the division (`StackedDividedDamageTriggerService`, CR 603.3d), kept on "
         "the entry; the sacrifice is chosen as it resolves. Does NOT use standard targeting |"),
        ("from `StackEntry.damageAssignments` (or `pendingETBDamageAssignments` when `etbAssignments`);",
         "from `StackEntry.damageAssignments` (a triggered ability with `etbAssignments` announces it as it is put on "
         "the stack, CR 603.3d);"),
        ("(CR 601.2c-d, CR 603.3d); a staged `pendingETBDamageAssignments` division is checked and taken over.",
         "(CR 601.2c-d, CR 603.3d)."),
        ("| `()`, `(boolean requiresImprintedXCost)`, or `boundTo(UUID imprintedCardId)` on an instance |",
         "| `()`, `(boolean requiresImprintedXCost)` |"),
        ("`boundTo(...)` pins the effect to one exiled card (`boundImprintedCardId`), so a delayed trigger keeps "
         "copying the card it exiled even if the spell is cast again and imprints another; the registration handler "
         "binds it (Surge to Victory).",
         "A stack entry with a linked exiled card copies that card: a `RegisterGlobalTriggeredAbilityUntil…` "
         "registration records the card its source exiled (`TemporaryGlobalTriggeredAbility.linkedExiledCardId`) "
         "and the collector puts it on the entry, so a delayed trigger keeps copying the card it exiled even if the "
         "spell is cast again and exiles another (Surge to Victory)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("Surge to Victory's copies are pinned with `CopyImprintedCardAndMayCastCopyEffect.boundTo(id)`, which the "
         "registration handler applies to the card the spell imprinted, so recasting the spell does not change the "
         "copied card.",
         "the registration records the card the spell exiled (`TemporaryGlobalTriggeredAbility.linkedExiledCardId`) "
         "and the collector puts it on the entry as its linked exiled card, which `CopyImprintedCardAndMayCastCopyEffect` "
         "copies, so recasting the spell does not change Surge to Victory's copied card."),
    ],
    "CARD_PATTERNS_CREATURES_ETB.md": [
        ("plus {R} BoostSelfEffect(1,0) activated ability. Damage assignments via `pendingETBDamageAssignments` |",
         "plus {R} BoostSelfEffect(1,0) activated ability. Targets and division announced as each ability goes on "
         "the stack (`StackedDividedDamageTriggerService`) |"),
        ("Damage assignments via `pendingETBDamageAssignments`, artifact choice via "
         "PermanentChoiceContext.SacrificeArtifactForDividedDamage |",
         "Targets (zero or more) and division announced as the ability goes on the stack "
         "(`StackedDividedDamageTriggerService`), artifact choice as it resolves via "
         "PermanentChoiceContext.SacrificeArtifactForDividedDamage |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("A cast that only a raw `GameService` overload covers goes through\n"
         "`harness.castDeclaringEtbTargets(...)`, which hands the call the targets the engine should get.\n",
         "A cast that only a raw `GameService` overload covers goes through\n"
         "`harness.castDeclaringEtbTargets(...)`, which hands the call the targets the engine should get.\n"
         "A divided triggered ability (\"N damage divided as you choose among …\", a counter distribution, divided\n"
         "prevention) announces its targets and division as it is put on the stack, never from anything staged\n"
         "before it triggered; a test declares them with `harness.declareDivision(Map.of(target, amount, …))`, which\n"
         "answers the ability's target choice and per-target amounts once they open (`DeclaredDivisions`, one\n"
         "declaration per announced division, an empty map choosing no target). A card whose divided ability has a\n"
         "declared target group takes its targets from the cast helper's target list as above.\n"),
    ],
    "ARCHITECTURE.md": [
        ("A staged `pendingETBDamageAssignments` buffer is taken over (a staged death-trigger counter distribution "
         "only once its recipients are legal targets and its amounts add up); the targets",
         "Kuldotha Flamefiend's `SacrificeArtifactThenDealDividedDamageEffect` (also inside a `MayEffect`) announces "
         "its zero or more targets the same way (`StackedDividedDamageTargets.allowsNoTargets`; zero targets is "
         "`StackEntry.divisionAnnounced` with an empty division). Nothing staged before the ability triggered is "
         "taken over; a divided enter ability with a declared target group (Fury, Bogardan Hellkite, Verdurous "
         "Gearhulk) chooses its targets through that group and divides among them in `ETBTokenTargetService`. The "
         "targets"),
    ],
}

REMOVED = ["pendingETBDamageAssignments", "boundImprintedCardId", "boundTo("]


def main() -> None:
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text(encoding="utf-8")
        original = text
        for old, new in edits:
            applied = new in text if old in new else old not in text
            if applied:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected one occurrence of {old[:70]!r}, found {text.count(old)}")
            text = text.replace(old, new)
        if text != original:
            path.write_text(text, encoding="utf-8")
            print("updated", name)
    for path in sorted(DOCS.glob("*.md")):
        text = path.read_text(encoding="utf-8")
        for form in REMOVED:
            for line in text.splitlines():
                if form in line and "Gone" not in line and "gone" not in line:
                    raise SystemExit(f"{path.name} still names {form}: {line[:120]}")


if __name__ == "__main__":
    main()
