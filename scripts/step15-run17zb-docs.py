#!/usr/bin/env python3
"""Step 15, run 17zb: the agent-docs lines that name the emblems' removed enter and tap markers and Sunbird's
Invocation's and Kiora, Sovereign of the Deep's removed cast markers, or their changed forms.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
marker outside a "Gone" line.

Usage: step15-run17zb-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17zb"
ENTERS_READER = ("read by the emblem enters reader (`TriggerCollectionService.checkEmblemEntersTriggers`) from the "
                 "command zone (CR 114.4); an emblem isn't a permanent, so Ancient Greenwarden or Panharmonicon "
                 "doesn't make it trigger an additional time")
LUKKA = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), "
         "DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower())), \"…\")`")
CHANDRA = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL), "
           "new DealDamageToAnyTargetEffect(3)), \"…\")`")
KOTH = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), "
        "ObjectRelation.YOU_CONTROL), new DealDamageToAnyTargetEffect(4)), \"…\")`")
NISSA = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), "
         "new MayEffect(new DrawCardEffect(1), \"Draw a card?\")), \"…\")`")
KIORA = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), "
         "new MayEffect(new EnteringCreatureFightsTargetCreatureEffect(), \"Have it fight target creature?\")), \"…\")`")
TEZZERET = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.permanentYouControlBecomesTapped().withFilter("
            "Triggers.ARTIFACT), new DrawCardEffect(1)), \"…\")`")
KIORA_SOVEREIGN = ("`Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(Kraken, Leviathan, Octopus, Serpent "
                   "CardSubtypePredicate`s)).castFrom(Zone.HAND)` + `KioraSovereignOfTheDeepRevealAndCastEffect()`")
SUNBIRD = "`Triggers.youCastSpell().castFrom(Zone.HAND)` + `SunbirdsInvocationRevealAndCastEffect()`"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"

EDITS = [
    (QR, "- `KioraSovereignOfTheDeepTriggerEffect()` + `KioraSovereignOfTheDeepRevealAndCastEffect(int spellManaValue)` —",
     "- " + KIORA_SOVEREIGN + " — Kiora, Sovereign of the Deep: when a qualifying sea-monster spell is cast from hand, "
     "privately look at the top X cards, X the triggering spell's mana value read as the ability resolves (the live "
     "spell, else as it last existed on the stack), and offer one nonland spell with strictly lower mana value for a "
     "free cast; unchosen cards go to the bottom in random order. `KioraSovereignOfTheDeepTriggerEffect` is **Gone** ("
     + RUN + ").", "line"),
    (QR, "- `DealDamageToAnyTargetOnControllerArtifactEntersEffect(int damage)` —",
     "- `DealDamageToAnyTargetOnControllerArtifactEntersEffect` / `EmblemArtifactEntersTriggerEffect` — **Gone** (" + RUN
     + "): Chandra, Spark Hunter's emblem is " + CHANDRA + ", " + ENTERS_READER + ".", "line"),
    (QR, "- `EmblemArtifactTapTriggerEffect()` —",
     "- `EmblemArtifactTapTriggerEffect` — **Gone** (" + RUN + "): Tezzeret, Betrayer of Flesh's emblem is " + TEZZERET
     + ", read by the emblem tap reader (`TriggerCollectionService.collectEmblemTapTriggers`): a \"becomes tapped\" "
     "ability of the emblem, its trigger's relation and filters judged on the tapped permanent, no options and no "
     "target.", "line"),
    (QR, "- `DrawCardOnAllyLandEntersEffect` (capability interface; use `.Marker()`) —",
     "- `DrawCardOnAllyLandEntersEffect` — **Gone** (" + RUN + "): Nissa, Vital Force's emblem is " + NISSA + ", "
     + ENTERS_READER + "; the \"you may\" is asked as the ability resolves (CR 603.5).", "line"),
    (QR, "- `DealDamageToAnyTargetOnAllyLandEntersEffect(CardSubtype landSubtype, int damage)` —",
     "- `DealDamageToAnyTargetOnAllyLandEntersEffect` — **Gone** (" + RUN + "): Koth, Fire of Resistance's emblem is "
     + KOTH + ", " + ENTERS_READER + ".", "line"),
    (QR, "- `MayFightTargetCreatureOnAllyCreatureEntersEffect` (capability interface; use `.Marker()`) —",
     "- `MayFightTargetCreatureOnAllyCreatureEntersEffect` / `DealDamageToAnyTargetOnAllyCreatureEntersEffect` — "
     "**Gone** (" + RUN + "): Kiora, Master of the Depths' emblem is " + KIORA + " and Lukka, Wayward Bonder's " + LUKKA
     + " (the creature deals the damage), " + ENTERS_READER + "; the entering permanent is the entry's triggering "
     "permanent, a target is chosen as the ability goes on the stack.", "line"),
    (QR, "- `CreateEmblemEffect(TriggeredAbility, String reminderText)` —",
     None, "fragment:(a draw trigger is read by `DrawService`, `Triggers.playerDrawsCard()`)=>(a draw trigger is read "
     "by `DrawService`, `Triggers.playerDrawsCard()`; \"whenever a [filter] you control enters\" by the emblem enters "
     "reader, `Triggers.enters(filter, ObjectRelation.YOU_CONTROL)`; \"whenever a [filter] you control becomes tapped\" "
     "by the emblem tap reader, `Triggers.permanentYouControlBecomesTapped().withFilter(filter)`, " + RUN + ")"),
    (IX, "| `KioraSovereignOfTheDeepTriggerEffect` / `KioraSovereignOfTheDeepRevealAndCastEffect` |",
     "| `KioraSovereignOfTheDeepRevealAndCastEffect` | `()` | Kiora, Sovereign of the Deep writes " + KIORA_SOVEREIGN
     + ". Privately looks at the top X cards, X the triggering spell's mana value read as the ability resolves (the "
     "live spell, else as it last existed on the stack), offers one nonland spell with strictly lower mana value for a "
     "free cast, and puts the rest on the bottom in random order. Reuses the `CAST_WITHOUT_PAYING` library-search "
     "flow. `KioraSovereignOfTheDeepTriggerEffect` is **Gone** (" + RUN + ") |", "line"),
    (IX, "| `DealDamageToAnyTargetOnControllerArtifactEntersEffect` |",
     "| `DealDamageToAnyTargetOnControllerArtifactEntersEffect` | — | **Gone** (" + RUN + "): Chandra, Spark Hunter's "
     "emblem is " + CHANDRA + ", " + ENTERS_READER + " |", "line"),
    (IX, "| `SunbirdsInvocationTriggerEffect` |",
     "| `SunbirdsInvocationTriggerEffect` | — | **Gone** (" + RUN + "): Sunbird's Invocation writes " + SUNBIRD + " |",
     "line"),
    (IX, "| `SunbirdsInvocationRevealAndCastEffect` |",
     "| `SunbirdsInvocationRevealAndCastEffect` | `()` | Reveal top X cards, X the triggering spell's mana value read "
     "as the ability resolves (the live spell, else as it last existed on the stack; `TargetSpellManaValue`). "
     "Controller may cast a non-land spell with MV ≤ X from among revealed without paying its mana cost. Remaining "
     "cards go to bottom in random order (no player reorder). Uses `LIBRARY_SEARCH` with `CAST_WITHOUT_PAYING` "
     "destination. Sunbird's Invocation writes " + SUNBIRD + " |", "line"),
    (IX, "| `EmblemArtifactTapTriggerEffect` |",
     "| `EmblemArtifactTapTriggerEffect` | — | **Gone** (" + RUN + "): Tezzeret, Betrayer of Flesh's emblem is "
     + TEZZERET + ", read by the emblem tap reader |", "line"),
    (IX, "| `DrawCardOnAllyLandEntersEffect` |",
     "| `DrawCardOnAllyLandEntersEffect` | — | **Gone** (" + RUN + "): Nissa, Vital Force's emblem is " + NISSA + ", "
     + ENTERS_READER + " |", "line"),
    (IX, "| `DealDamageToAnyTargetOnAllyLandEntersEffect` |",
     "| `DealDamageToAnyTargetOnAllyLandEntersEffect` | — | **Gone** (" + RUN + "): Koth, Fire of Resistance's emblem "
     "is " + KOTH + ", " + ENTERS_READER + " |", "line"),
    (IX, "| `MayFightTargetCreatureOnAllyCreatureEntersEffect` |",
     "| `MayFightTargetCreatureOnAllyCreatureEntersEffect` / `DealDamageToAnyTargetOnAllyCreatureEntersEffect` | — | "
     "**Gone** (" + RUN + "): Kiora, Master of the Depths' emblem is " + KIORA + ", Lukka, Wayward Bonder's " + LUKKA
     + ", " + ENTERS_READER + " |", "line"),
    (OM, "| \"Whenever you cast a Kraken, Leviathan, Octopus, or Serpent spell from your hand, look at the top X cards",
     "| \"Whenever you cast a Kraken, Leviathan, Octopus, or Serpent spell from your hand, look at the top X cards of "
     "your library, where X is that spell's mana value. You may cast a spell with mana value less than X from among "
     "them without paying its mana cost. Put the rest on the bottom of your library in a random order.\" | "
     + KIORA_SOVEREIGN + " | Triggers.youCastSpell() | Kiora, Sovereign of the Deep. X is the triggering spell's mana "
     "value as the ability resolves; the look is private, only nonland cards strictly below X are offered, and every "
     "unchosen looked-at card is bottomed randomly |", "line"),
    (OM, "| \"−8: You get an emblem with \\\"Whenever a creature you control enters, you may have it fight target creature.",
     "| \"−8: You get an emblem with \\\"Whenever a creature you control enters, you may have it fight target "
     "creature.\\\" Then create three 8/8 blue Octopus creature tokens.\" | " + KIORA + " + `CreateTokenEffect(3, "
     "Octopus, 8/8, BLUE)` | loyalty ability | Kiora, Master of the Depths (`BFZ` 213). The emblem enters reader puts "
     "the ability on the stack with the entering creature as its triggering permanent, its target chosen as it goes "
     "on the stack and the fight asked as it resolves |", "line"),
    (OM, "| \"you get an emblem with \\\"whenever an artifact you control becomes tapped, draw a card\\\"\" |",
     "| \"you get an emblem with \\\"whenever an artifact you control becomes tapped, draw a card\\\"\" | " + TEZZERET
     + " | loyalty ability | Tezzeret, Betrayer of Flesh −6. The emblem tap reader judges the trigger's filter on the "
     "tapped permanent |", "line"),
    (OM, "| \"−8: You get an emblem with \\\"Whenever a creature you control enters, you may have it fight target creature.",
     "| \"you get an emblem with \\\"whenever a [filter] you control enters, this emblem deals N damage to any target\\\" "
     "/ \\\"… it deals damage equal to its power to any target\\\" / \\\"… you may draw a card\\\"\" | " + CHANDRA
     + " (Chandra, Spark Hunter), " + KOTH + " (Koth, Fire of Resistance), " + LUKKA + " (Lukka, Wayward Bonder), "
     + NISSA + " (Nissa, Vital Force) | loyalty ability | " + ENTERS_READER + " |", "after"),
]

GONE = ["DealDamageToAnyTargetOnAllyCreatureEntersEffect", "DealDamageToAnyTargetOnControllerArtifactEntersEffect",
        "EmblemArtifactEntersTriggerEffect", "DealDamageToAnyTargetOnAllyLandEntersEffect",
        "MayFightTargetCreatureOnAllyCreatureEntersEffect", "DrawCardOnAllyLandEntersEffect",
        "EmblemArtifactTapTriggerEffect", "SunbirdsInvocationTriggerEffect", "KioraSovereignOfTheDeepTriggerEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after":
        if new in lines:
            return False
        if not idx:
            raise SystemExit(f"anchor not found: {start}")
        lines.insert(idx[0] + 1, new)
        return True
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        if any(repl in line for line in lines) and not any(old in lines[i] for i in idx):
            return False
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
    by_file = {}
    for path, start, new, mode in EDITS:
        by_file.setdefault(path, []).append((start, new, mode))
    for path, edits in by_file.items():
        file = ROOT / path
        lines = file.read_text().split("\n")
        changed = False
        for start, new, mode in edits:
            changed |= apply(lines, start, new, mode)
        if changed:
            file.write_text("\n".join(lines))
            print(f"updated {path}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("SKILL.md")) \
        + list((ROOT / ".codex/skills").rglob("SKILL.md"))
    for doc in docs:
        for number, line in enumerate(doc.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{doc.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
