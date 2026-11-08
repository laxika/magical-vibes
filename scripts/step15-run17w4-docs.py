#!/usr/bin/env python3
"""Step 15, run 17w4: the agent-docs rows of the copy-for-each cast triggers.

Rewrites the rows that described the seven copy-for-each records as trigger descriptors the spell-cast collector
populates with the spell's snapshot, caster and original target (`CopySpellForEachOtherPermanentOrPlayerEffect`'s
`(StackEntry, UUID, UUID)` form, `CopySpellForEachOtherPlayerEffect`'s spell filter, "resolution snapshot populated at
trigger time"): each is now a filtered cast trigger whose copy effect reads the stack entry's triggering spell, its
target conditions the trigger's filters. Adds `StackEntryHasOneTargetPredicate` and
`StackEntryCouldTargetAnotherPermanentPredicate` to `PREDICATES_REFERENCE.md` and a sentence to `ARCHITECTURE.md`'s
triggering-spell paragraph, and marks `EFFECT_MERGE_PLAN.md` step 25's target without the snapshot components.
Idempotent. Fails if a doc or skill still names a removed form.

Usage: step15-run17w4-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

IOS = "an instant-or-sorcery `withFilter`"
ONLY_THIS = ("`withStackEntryFilter(StackEntryAllOfPredicate(StackEntryTargetsOnlySinglePermanentOrPlayerPredicate, "
             "StackEntryTargetsSourcePredicate))` (\"targets only this creature\")")
READS = ("as it resolves the effect reads the stack entry's triggering spell (the spell while it is still on the stack, "
         "else as it last existed there, CR 608.2h)")

# file -> [(old line prefix, new line)]: the line starting with the prefix is replaced whole.
LINES = {
    "EFFECTS_INDEX.md": [
        ("| `CopySpellForEachOtherPermanentOrPlayerEffect` |",
         "| `CopySpellForEachOtherPermanentOrPlayerEffect` | `()` / `permanentsAndPlayersOnly()` | copies a single-target "
         "spell for each other legal target. As a SPELL or targeted ability effect it copies the target spell or ability "
         "under the resolving effect's controller; `permanentsAndPlayersOnly()` also accepts activated or triggered "
         "abilities and limits copies to permanents and players (Radiate, Radiant Performer). As a cast trigger's effect "
         "(Glimmervoid Basin: `Triggers.playerCastsSpell()` with " + IOS + " and `withStackEntryFilter(new "
         "StackEntryHasOneTargetPredicate())`) the generic cast collector aims the entry at the triggering spell without "
         "targeting it; " + READS + ", the copies include other legal spell/card zones and the caster controls them |"),
        ("| `CopySpellForEachOtherPlayerEffect` |",
         "| `CopySpellForEachOtherPlayerEffect` | `()` mandatory / `(boolean optional)` | \"each other player copies that "
         "spell\" of a filtered cast trigger: " + READS + " (so targets changed in the meantime carry over), and each "
         "player other than its controller copies it and may choose new targets. `optional=true` makes each other player "
         "*may* copy (a \"you may\" per player that, on accept, creates one copy via `CopyControllerCastSpellEffect`). "
         "The trigger's filter says which spells: Hive Mind = `Triggers.playerCastsSpell().withStackEntryFilter("
         "StackEntryTypeInPredicate(INSTANT_SPELL, SORCERY_SPELL))` + `new CopySpellForEachOtherPlayerEffect()`; Curse of "
         "Echoes = `withStackEntryFilter(StackEntryAllOfPredicate(typeFilter, StackEntryControlledByEnchantedPlayerPredicate))` "
         "+ `new CopySpellForEachOtherPlayerEffect(true)` |"),
        ("| `CopySpellForEachOtherControlledCreatureEffect` |",
         "| `CopySpellForEachOtherControlledCreatureEffect` | `()` / `(boolean chooseOne)` | \"that player copies that "
         "spell for each other creature they control that the spell could target\" of a filtered cast trigger "
         "(`Triggers.playerCastsSpell()` / `youCastSpell()` with " + IOS + " and " + ONLY_THIS + "): " + READS + "; the "
         "spell's controller controls the copies, each targeting a different one of their other creatures (Mirrorwing "
         "Dragon, Zada). `chooseOne`: the caster chooses one such creature and makes one copy (Beamsplitter Mage, whose "
         "intervening \"if\" is `StackEntryCouldTargetAnotherPermanentPredicate(creature you control, false)` in the "
         "trigger's filter; no copy when none is left as it resolves) |"),
        ("| `CopySpellForEachOtherCreatureEffect` |",
         "| `CopySpellForEachOtherCreatureEffect` | `()` | \"if that spell targets only this creature, copy the spell for "
         "each other creature that spell could target\" of a filtered cast trigger (`Triggers.playerCastsSpell()` with "
         + IOS + " and " + ONLY_THIS + " as the intervening \"if\"): " + READS + ", checks the \"if\" again (CR 603.4) and "
         "makes the copies under the ability's controller, target legality judged from the original caster. Used by "
         "Ink-Treader Nephilim |"),
        ("| `CopySpellForEachOtherCreatureWithManaEffect` |",
         "| `CopySpellForEachOtherCreatureWithManaEffect` | `(String manaCost)` | `Triggers.youCastSpell().withFilter("
         "noncreature)` with " + ONLY_THIS + ": " + READS + "; the controller chooses any number of other legal "
         "creatures, pays `manaCost` for each, and creates a copy targeting each chosen creature; copies of permanent "
         "spells become tokens. Used by Feather, Radiant Arbiter |"),
        ("| `CopySpellForEachOtherSubtypePermanentEffect` |",
         "| `CopySpellForEachOtherSubtypePermanentEffect` | `(CardSubtype subtype)` | \"that player copies that spell for "
         "each other [subtype] that spell could target\" of a filtered cast trigger (`Triggers.playerCastsSpell()` with "
         + IOS + " and `withStackEntryFilter(StackEntryAllOfPredicate(StackEntryTargetsOnlySinglePermanentOrPlayerPredicate, "
         "StackEntryTargetsPermanentPredicate(PermanentHasSubtypePredicate(subtype))))`): " + READS + "; each copy "
         "targets a different other permanent of that subtype (other than the one the spell targeted as the ability "
         "triggered), under the spell's controller. Used by Precursor Golem |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CopySpellForEachOtherControlledCreatureEffect()` —",
         "- `CopySpellForEachOtherControlledCreatureEffect()` — `Triggers.playerCastsSpell()` with an instant-or-sorcery "
         "filter and \"targets only this creature\" (`StackEntryTargetsOnlySinglePermanentOrPlayerPredicate` + "
         "`StackEntryTargetsSourcePredicate`) as its stack-entry filter: the caster copies the triggering spell for each "
         "other creature they control that it could target (Mirrorwing Dragon; Zada on `youCastSpell()`); `(true)` is "
         "one copy onto a chosen creature (Beamsplitter Mage, its intervening \"if\" "
         "`StackEntryCouldTargetAnotherPermanentPredicate`)"),
        ("- `CopySpellForAnotherOpponentPermanentEffect()` —",
         "- `CopySpellForAnotherOpponentPermanentEffect()` — `Triggers.youCastSpell()` with an instant-or-sorcery filter "
         "and \"targets only a single nonland permanent an opponent controls\" "
         "(`StackEntryTargetsOnlySinglePermanentOrPlayerPredicate` + `StackEntryTargetsPermanentPredicate`) plus the "
         "intervening \"if\" `StackEntryCouldTargetAnotherPermanentPredicate(nonland an opponent controls, true)` as its "
         "stack-entry filter: choose a nonland permanent another opponent controls that the triggering spell could target "
         "and create one copy targeting it (Exterminator Magmarch)"),
        ("- `CopySpellForEachOtherSubtypePermanentEffect(CardSubtype)` —",
         "- `CopySpellForEachOtherSubtypePermanentEffect(CardSubtype)` — `Triggers.playerCastsSpell()` with an "
         "instant-or-sorcery filter and \"targets only a single [subtype]\" (`StackEntryTargetsOnlySinglePermanentOrPlayerPredicate` "
         "+ `StackEntryTargetsPermanentPredicate(PermanentHasSubtypePredicate)`): copy the triggering spell for each other "
         "matching permanent it could target (Precursor Golem)"),
        ("- `CopySpellForEachOtherPermanentOrPlayerEffect()` —",
         "- `CopySpellForEachOtherPermanentOrPlayerEffect()` — SPELL: copy the targeted single-target spell for each other "
         "legal target, including non-battlefield cards when the original spell allows them. `permanentsAndPlayersOnly()` "
         "also accepts activated/triggered abilities and limits copies to other permanents and players (Radiate, Radiant "
         "Performer). Copies are controlled by the resolving effect's controller. As a cast trigger's effect "
         "(`Triggers.playerCastsSpell()` with an instant-or-sorcery filter and `StackEntryHasOneTargetPredicate`, "
         "Glimmervoid Basin) it copies the stack entry's triggering spell and the caster controls the copies."),
        ("- `CopySpellForEachOtherCreatureEffect()` -",
         "- `CopySpellForEachOtherCreatureEffect()` - `Triggers.playerCastsSpell()` with an instant-or-sorcery filter and "
         "\"targets only this creature\" as its stack-entry filter (the intervening \"if\", checked again as it resolves): "
         "the ability's controller copies the triggering spell for each other creature it could target; legality uses the "
         "original spell caster (Ink-Treader Nephilim)"),
        ("- `CopySpellForEachOtherCreatureWithManaEffect(\"{2}\")` -",
         "- `CopySpellForEachOtherCreatureWithManaEffect(\"{2}\")` - `Triggers.youCastSpell()` with a noncreature filter "
         "and \"targets only this creature\" as its stack-entry filter: the controller chooses any number of other legal "
         "creatures, pays the configured generic cost for each, and creates a copy of the triggering spell targeting each "
         "chosen creature; permanent-spell copies become tokens (Feather, Radiant Arbiter)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a player casts an instant or sorcery spell with a single target, that player copies that spell for each other spell, permanent, card not on the battlefield, and/or player the spell could target\" |",
         "| \"Whenever a player casts an instant or sorcery spell with a single target, that player copies that spell for "
         "each other spell, permanent, card not on the battlefield, and/or player the spell could target\" | "
         "`CopySpellForEachOtherPermanentOrPlayerEffect()` | `Triggers.playerCastsSpell().withFilter(instant or sorcery)"
         ".withStackEntryFilter(new StackEntryHasOneTargetPredicate())` | \"a single target\" is one target chosen once "
         "(a spell naming the same object twice doesn't trigger it). The entry is aimed at the triggering spell without "
         "targeting it; the handler copies it (live, else as it last existed) for each of its other legal spell, "
         "permanent, graveyard, exile and player targets; copies are controlled by the caster. Glimmervoid Basin |"),
        ("| \"whenever a player casts an instant or sorcery spell that targets only this creature, copy the spell for each other creature that spell could target\" |",
         "| \"whenever a player casts an instant or sorcery spell, if that spell targets only this creature, copy the spell "
         "for each other creature that spell could target\" | `CopySpellForEachOtherCreatureEffect()` | "
         "`Triggers.playerCastsSpell()` with an instant-or-sorcery filter and `withStackEntryFilter(StackEntryAllOfPredicate("
         "StackEntryTargetsOnlySinglePermanentOrPlayerPredicate, StackEntryTargetsSourcePredicate))` | Ink-Treader "
         "Nephilim - the filter is the intervening \"if\", which the effect checks again as it resolves (CR 603.4); copies "
         "are controlled by the Nephilim's controller, while target legality is checked from the original spell caster's "
         "perspective |"),
        ("| \"Whenever you cast a noncreature spell that targets only Feather,",
         "| \"Whenever you cast a noncreature spell that targets only Feather, you may choose any number of other "
         "creatures that spell could target and pay {2} for each of those creatures. If you do, for each of those "
         "creatures, copy that spell. The copy targets that creature.\" | `CopySpellForEachOtherCreatureWithManaEffect(\"{2}\")` "
         "| `Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CREATURE)))` with \"targets only "
         "this creature\" as its stack-entry filter | Feather, Radiant Arbiter (MKC 6) - the controller chooses any number "
         "of other legal creatures at trigger resolution, pays {2} per chosen creature, and copies of permanent spells "
         "become tokens |"),
        ("| \"whenever a player casts an instant or sorcery spell that targets only this creature, that player copies that spell for each other creature they control that the spell could target\" |",
         "| \"whenever a player casts an instant or sorcery spell that targets only this creature, that player copies that "
         "spell for each other creature they control that the spell could target\" | "
         "`CopySpellForEachOtherControlledCreatureEffect()` | `Triggers.playerCastsSpell()` with an instant-or-sorcery "
         "filter and `withStackEntryFilter(StackEntryAllOfPredicate(StackEntryTargetsOnlySinglePermanentOrPlayerPredicate, "
         "StackEntryTargetsSourcePredicate))` | Mirrorwing Dragon — copies only for other creatures controlled by the "
         "spell's caster (not all creatures / not the Dragon's controller). Zada is the same on `youCastSpell()`; "
         "Beamsplitter Mage's \"if you control one or more other creatures that spell could target, choose one of those "
         "creatures. Copy that spell\" is `(true)` with `StackEntryCouldTargetAnotherPermanentPredicate(creature you "
         "control, false)` added to the filter |"),
        ("| \"whenever a player casts an instant or sorcery spell that targets only a single [subtype], that player copies that spell for each other [subtype] that spell could target\" |",
         "| \"whenever a player casts an instant or sorcery spell that targets only a single [subtype], that player copies "
         "that spell for each other [subtype] that spell could target\" | `CopySpellForEachOtherSubtypePermanentEffect("
         "CardSubtype.X)` | `Triggers.playerCastsSpell()` with an instant-or-sorcery filter and `withStackEntryFilter("
         "StackEntryAllOfPredicate(StackEntryTargetsOnlySinglePermanentOrPlayerPredicate, StackEntryTargetsPermanentPredicate("
         "PermanentHasSubtypePredicate(X))))` | Precursor Golem — copies for every other matching permanent the spell "
         "could target (any controller) |"),
        ("| \"whenever a player casts an instant or sorcery spell, each other player copies that spell\" |",
         "| \"whenever a player casts an instant or sorcery spell, each other player copies that spell\" | "
         "`CopySpellForEachOtherPlayerEffect()` | `Triggers.playerCastsSpell().withStackEntryFilter(new "
         "StackEntryTypeInPredicate(Set.of(INSTANT_SPELL, SORCERY_SPELL)))` | Hive Mind (mandatory) |"),
        ("| \"whenever enchanted player casts an instant or sorcery spell, each other player may copy that spell\" |",
         "| \"whenever enchanted player casts an instant or sorcery spell, each other player may copy that spell\" | "
         "`CopySpellForEachOtherPlayerEffect(true)` | `Triggers.playerCastsSpell().withStackEntryFilter(StackEntryAllOfPredicate("
         "StackEntryTypeInPredicate(INSTANT_SPELL, SORCERY_SPELL), StackEntryControlledByEnchantedPlayerPredicate))` | "
         "Curse of Echoes — the filter gates to casts by the enchanted player; each other player *may* copy |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| single-copy trigger for a spell targeting one opponent's nonland permanent,",
         "| single-copy trigger for a spell targeting one opponent's nonland permanent, choosing a legal nonland permanent "
         "controlled by another opponent | `e/ExterminatorMagmarch.java` + `CopySpellForAnotherOpponentPermanentEffect` "
         "(the trigger's filters say \"targets only a single nonland permanent an opponent controls\" and the intervening "
         "\"if\", `StackEntryCouldTargetAnotherPermanentPredicate`) |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Curse (enchant player + copy their instants/sorceries) |",
         "| Curse (enchant player + copy their instants/sorceries) | `c/CurseOfEchoes.java` | "
         "Triggers.playerCastsSpell().withStackEntryFilter(StackEntryAllOfPredicate(StackEntryTypeInPredicate(INSTANT_SPELL, "
         "SORCERY_SPELL), StackEntryControlledByEnchantedPlayerPredicate)) + CopySpellForEachOtherPlayerEffect(true) — "
         "enchant player curse; Hive Mind's copy effect, the trigger's filter restricts it to instant/sorcery spells cast "
         "by the enchanted player; each other player *may* copy and may choose new targets |"),
    ],
}

PREDICATE_ANCHOR = "| `StackEntryIsSingleTargetPredicate` |"
PREDICATE_ROWS = [
    "| `StackEntryHasOneTargetPredicate` | `()` | spells/abilities with a single target: one target, chosen once (a spell "
    "naming the same object twice doesn't match, unlike `StackEntryIsSingleTargetPredicate`). Glimmervoid Basin's \"an "
    "instant or sorcery spell with a single target\" |",
    "| `StackEntryCouldTargetAnotherPermanentPredicate` | `(PermanentPredicate filter, boolean otherThanTargetController)` "
    "| spells that could target a permanent matching `filter` (judged from the ability's controller) other than the "
    "permanents they target; with `otherThanTargetController`, one controlled by a player other than its target's "
    "controller. The intervening \"if\" of a copy-onto-another-target cast trigger as its stack-entry filter (Beamsplitter "
    "Mage, Exterminator Magmarch); judged only by the spell-cast trigger collector, which enumerates the spell's legal "
    "targets |",
]

ARCH_ANCHOR = "`RemoveCounterFromSourceThenEffect`'s reflexive entry copies it."
ARCH_ADDED = (" The copy-for-each effects of a cast trigger (Mirrorwing Dragon, Precursor Golem, Hive Mind, Glimmervoid "
              "Basin and the like) read it through `CopySupport.triggeringSpell`, their \"targets only …\" conditions the "
              "trigger's stack-entry filters.")

MERGE_OLD = ("**Target**: `CopySpellForEachOtherMatchingPermanentEffect(PermanentPredicate filter, StackEntry spellSnapshot, "
             "UUID castingPlayerId, UUID originalTargetId)`.")
MERGE_NEW = ("**Target**: `CopySpellForEachOtherMatchingPermanentEffect(PermanentPredicate filter)` (the effects read the "
             "triggering spell from the stack entry since effect-slot-refactor.md step 15, run 17w4).")

# Forms that are gone: a regex each, none may be left in a doc or skill.
GONE = [
    r"CopySpellForEachOtherPermanentOrPlayerEffect` \| `\(\) / `permanentsAndPlayersOnly\(\)` / `\(StackEntry",
    r"CopySpellForEachOtherPlayerEffect\(true, new StackEntryControlledByEnchantedPlayerPredicate",
    r"CopySpellForEachOtherPlayerEffect\(typeFilter\)",
    r"CopySpellForEachOtherPlayerEffect\(true, StackEntryAllOfPredicate",
    r"\(StackEntry, UUID, UUID\)` form",
]


def rewrite_lines(path, edits):
    lines = path.read_text().split("\n")
    for prefix, new in edits:
        if new in lines:
            continue
        hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if len(hits) != 1:
            raise SystemExit(f"{path.name}: {len(hits)} lines start with {prefix!r}")
        lines[hits[0]] = new
    path.write_text("\n".join(lines))


def main():
    for name, edits in LINES.items():
        rewrite_lines(DOCS / name, edits)
    path = DOCS / "PREDICATES_REFERENCE.md"
    lines = path.read_text().split("\n")
    index = next(i for i, line in enumerate(lines) if line.startswith(PREDICATE_ANCHOR))
    for row in reversed(PREDICATE_ROWS):
        if row not in lines:
            lines.insert(index + 1, row)
    path.write_text("\n".join(lines))
    path = DOCS / "ARCHITECTURE.md"
    text = path.read_text()
    if ARCH_ADDED not in text:
        if text.count(ARCH_ANCHOR) != 1:
            raise SystemExit("ARCHITECTURE.md: the triggering-spell paragraph's sentence not found once")
        path.write_text(text.replace(ARCH_ANCHOR, ARCH_ANCHOR + ARCH_ADDED))
    path = DOCS / "EFFECT_MERGE_PLAN.md"
    path.write_text(path.read_text().replace(MERGE_OLD, MERGE_NEW))
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = path.read_text()
        for pattern in GONE:
            if re.search(pattern, text):
                raise SystemExit(f"{path.relative_to(ROOT)} still matches {pattern}")


if __name__ == "__main__":
    main()
