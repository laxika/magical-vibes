#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 17a: the agent-docs rows of the classes and cards the run changed.

Idempotent: each edit is an exact line replacement (or removal) that is skipped once the new text is there. Fails if
a removed class is still named in agent-docs afterwards.

    python3 scripts/step15-pipeline-cleanup-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

REMOVED = [
    "CreateTokensForNonlandCardsMilledEffect",
    "GainLifeEqualToCastSpellManaValueEffect",
    "MillTargetPlayerByCastSpellManaValueEffect",
    "SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect",
]

# (file, a unique substring of the old line, the new line or None to drop the line)
EDITS = [
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Creature card into opponent's graveyard → become a copy | `l/LazavDimirMastermind.java` |",
     "| Creature card into opponent's graveyard → become a copy | `l/LazavDimirMastermind.java` | Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS) MayEffect(BecomeCopyOfCreatureCardInOpponentGraveyardEffect()) — the effect copies the stack entry's triggering card as it was put into the graveyard (last-known information, no target, no re-check on resolution); the copy keeps Lazav's name, gains legendary + hexproof, and re-gains the trigger |"),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Spirit/Arcane spell cast trigger — may mill target player by mana value | `c/CloudhoofKirin.java` |",
     "| Spirit/Arcane spell cast trigger — may mill target player by mana value | `c/CloudhoofKirin.java` | Triggers.youCastSpell().withFilter(CardAnyOf(SPIRIT, ARCANE)) `MayEffect(MillEffect(new TargetSpellManaValue(), MillRecipient.TARGET_PLAYER))` — the player is chosen as the ability goes on the stack; \"that spell's mana value\" is read from the triggering spell as it resolves |"),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Spirit/Arcane spell cast trigger — may gain life by mana value | `b/BounteousKirin.java` |",
     "| Spirit/Arcane spell cast trigger — may gain life by mana value | `b/BounteousKirin.java` | Triggers.youCastSpell().withFilter(CardAnyOf(SPIRIT, ARCANE)) `MayEffect(GainLifeEffect(new TargetSpellManaValue()))` — \"that spell's mana value\" is read from the triggering spell (live, else as it last existed on the stack) as it resolves |"),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Opponent color spell +1/+1 counter (may) | `m/MoldAdder.java` |",
     "| Opponent color spell +1/+1 counter (may) | `m/MoldAdder.java` | Triggers.opponentCastsSpell().withFilter(CardAnyOf(CardColorPredicate(BLUE), CardColorPredicate(BLACK))) MayEffect(PutCountersOnSourceEffect(1, 1, 1)) — the spell's colors are the trigger's filter |"),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md",
     "| Same-name artifact cast search | `m/MishraArtificerProdigy.java` |",
     "| Same-name artifact cast search | `m/MishraArtificerProdigy.java` | Triggers.youCastSpell().withFilter(CardTypePredicate(ARTIFACT)) `MayEffect(SearchZonesForCardNamedToBattlefieldEffect.namedAsTriggeringSpell())` — the search names the stack entry's triggering spell as it resolves |"),
    ("EFFECTS_INDEX.md",
     "| `ManaEchoesEffect` |",
     "| `ManaEchoesEffect` | `()` | Mana Echoes, on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` in a `MayEffect`: adds {C} for each creature its controller controls that shares a creature type with the entering creature, read as it resolves (the creature on the battlefield, else as it last existed there, else as it entered) |"),
    ("EFFECTS_INDEX.md", "| `SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect` |", None),
    ("EFFECTS_INDEX.md", "| `MillTargetPlayerByCastSpellManaValueEffect` |", None),
    ("EFFECTS_INDEX.md", "| `GainLifeEqualToCastSpellManaValueEffect` |", None),
    ("EFFECTS_INDEX.md",
     "| `TriggeringSpellTargetCount` |",
     "| `TriggeringSpellTargetCount` | `(PermanentPredicate filter)` | `DynamicAmount` counting the targets of the spell that caused the current triggered ability that match the supplied permanent filter, as the ability resolves: the spell's targets while it is on the stack, else those it had as it last existed there (the stack entry's triggering spell); a filtered target that left the battlefield is judged as it last existed there. `TriggeringSpellTargetCount.allTargets()` counts every target occurrence, including players, spells, and graveyard cards |"),
    ("EFFECTS_INDEX.md",
     "| `SearchZonesForCardNamedToBattlefieldEffect` |",
     "| `SearchZonesForCardNamedToBattlefieldEffect` | `(String cardName, boolean includeHand, boolean attachToTarget[, List<String> additionalCardNames, Set<Zone> selectedZones])`; convenience `(cardName)` = `(cardName, true, false)`; `namedAsTriggeringSpell()` names the stack entry's triggering spell as it resolves | Searches named cards onto the battlefield. The attached-Aura variant first chooses graveyard, library, or both, then selects a match across the chosen zones; only a library search shuffles. `selectedZones` retains the decision. An illegal creature target prevents searching. The nonattached variant retains automatic graveyard/hand matches. Used by Gate to the Afterlife, Arachnus Spinner and Mishra, Artificer Prodigy (\"a card with the same name as that spell\"). |"),
    ("EFFECTS_INDEX.md",
     "(wrap in MayEffect for \"you may\" triggers like Mold Adder)",
     "| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | `(Set<CardColor> triggerColors, int amount, boolean onlyOwnSpells)` | put +1/+1 counters when spell of matching color is cast. Use `Triggers.youCastSpell()` with `onlyOwnSpells=true` for \"whenever you cast\" cards; use `Triggers.playerCastsSpell()` with `onlyOwnSpells=false` for \"whenever a player casts\" cards; use `Triggers.opponentCastsSpell()` with `onlyOwnSpells=false` for \"whenever an opponent casts\" cards. An empty `triggerColors` set means any spell, colorless ones included (Managorger Hydra). Don't wrap it in a `MayEffect`: a \"you may\" counter trigger is the cast trigger's color filter and `MayEffect(PutCountersOnSourceEffect(1, 1, n))` (Mold Adder) |"),
    ("EFFECTS_INDEX.md",
     "| `BecomeCopyOfCreatureCardInOpponentGraveyardEffect` |",
     "| `BecomeCopyOfCreatureCardInOpponentGraveyardEffect` | `()` | source permanent becomes a copy of the creature card just put into an opponent's graveyard from anywhere (the stack entry's triggering card, as it was put there), except its name stays the source's printed name, it gains the `LEGENDARY` supertype and the `HEXPROOF` keyword, and it re-gains the source's own `Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS)` ability. The copy uses last-known information (the ability doesn't target and doesn't re-check the graveyard on resolution). Wrap in `MayEffect`. Used by Lazav, Dimir Mastermind |"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ManaEchoesEffect()` —",
     "- `ManaEchoesEffect()` — on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` in a `MayEffect`: the controller adds one colorless mana for each creature they control sharing a creature type with the entering creature, read as it resolves (the creature on the battlefield, else as it last existed there, else as it entered)."),
    ("EFFECTS_QUICK_REFERENCE.md", "- `SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect()` —", None),
    ("EFFECTS_QUICK_REFERENCE.md",
     "  the current triggered ability matching the supplied permanent filter; the spell-cast collector",
     "  the current triggered ability matching the supplied permanent filter, counted as the ability"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "  snapshots the count when the trigger is created.",
     "  resolves from the spell, or from the triggering spell's snapshot once it has left the stack."),
    ("EFFECTS_QUICK_REFERENCE.md", "- `MillTargetPlayerByCastSpellManaValueEffect(CardPredicate spellFilter)` —", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `GainLifeEqualToCastSpellManaValueEffect(CardPredicate spellFilter)` —", None),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `CreateTokensForNonlandCardsMilledEffect(CreateTokenEffect tokenTemplate)` on",
     "- \"Whenever one or more nonland cards are milled, you may create that many … tokens. Do this only once each turn.\" — `MayEffect(new CreateTokenEffect(new EventValue(), …))` on `Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` with `TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`: the entry's event value is the number of nonland cards milled (Screeching Scorchbeast, PIP 49)."),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `SearchZonesForCardNamedToBattlefieldEffect(String cardName[, boolean includeHand, boolean attachToTarget])` —",
     "- `SearchZonesForCardNamedToBattlefieldEffect(String cardName[, boolean includeHand, boolean attachToTarget])` — searches named cards into the battlefield. The attached-Aura variant offers graveyard, library, or both before choosing a match; only searching the library shuffles it. `selectedZones` retains that choice. `attachToTarget=true` attaches the found card to the targeted creature; an illegal target stops the search (Arachnus Spinner → Arachnus Web). The nonattached variant retains automatic graveyard/hand matches (Gate to the Afterlife → God-Pharaoh's Gift). `namedAsTriggeringSpell()` searches for \"a card with the same name as that spell\", the stack entry's triggering spell (Mishra, Artificer Prodigy)."),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `BecomeCopyOfCreatureCardInOpponentGraveyardEffect()` —",
     "- `BecomeCopyOfCreatureCardInOpponentGraveyardEffect()` — source permanent becomes a copy of the creature card put into an opponent's graveyard from anywhere (the stack entry's triggering card), keeping its own name, gaining legendary + hexproof, and retaining its own trigger. Lazav, Dimir Mastermind. Place in `Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS)` wrapped in `MayEffect`"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature enters, you may add an amount of {C} equal to the number of creatures you control that share a creature type with it.\" |",
     "| \"Whenever a creature enters, you may add an amount of {C} equal to the number of creatures you control that share a creature type with it.\" | `MayEffect(ManaEchoesEffect())` on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` | triggered ability | Mana Echoes (ONS 218). The entering creature's types are read as the ability resolves (on the battlefield, else as it last existed there); the matching creatures are counted then, each once even if it shares several types. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever you cast an artifact spell, you may search your graveyard, hand, and/or library for a card with the same name as that spell and put it onto the battlefield. If you search your library this way, shuffle.\" |",
     "| \"Whenever you cast an artifact spell, you may search your graveyard, hand, and/or library for a card with the same name as that spell and put it onto the battlefield. If you search your library this way, shuffle.\" | `MayEffect(SearchZonesForCardNamedToBattlefieldEffect.namedAsTriggeringSpell())` | `Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT))` | Mishra, Artificer Prodigy (TSP). The search names the stack entry's triggering spell as it resolves |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "+ `new MayEffect(new CreateTokensForNonlandCardsMilledEffect(zombieMutant), ...)`",
     "| \"Whenever this creature attacks, each player gets two rad counters. Whenever one or more nonland cards are milled, you may create that many 2/2 black Zombie Mutant creature tokens. Do this only once each turn.\" | `GiveEachPlayerRadCounterEffect(2)` on `Triggers.attacks()` + `new MayEffect(zombieMutant, ...)` (a `CreateTokenEffect(new EventValue(), …)`) on `Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` with `TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` | attack + mill trigger | Screeching Scorchbeast (PIP 49); the entry's event value is the number of nonland cards milled. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever you cast a Spirit or Arcane spell, you may gain life equal to that spell's mana value\" |",
     "| \"Whenever you cast a Spirit or Arcane spell, you may gain life equal to that spell's mana value\" | `MayEffect(new GainLifeEffect(new TargetSpellManaValue()), prompt)` | `Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new CardSubtypePredicate(ARCANE))))` | Bounteous Kirin. \"That spell's mana value\" is read from the triggering spell as the ability resolves; the trigger is non-targeting |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "a non-empty set restricts to those colors (Mold Adder).",
     "| \"Whenever a player casts a spell, put a +1/+1 counter on this creature\" | `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false)` | `Triggers.playerCastsSpell()` | Managorger Hydra. An empty `triggerColors` set means any spell, colorless ones included; a non-empty set restricts to those colors (Titania's Chosen). Do NOT use `SpellCastTriggerEffect` + `PutCountersOnSourceEffect` — the generic collector only stamps `sourcePermanentId` for self-targeting effects, so the counter would never land |"),
]

ARCEE_OLD = "The back-face trigger counts matching targets at cast time, so spells with multiple controlled creature or Vehicle targets put that many counters on Arcee |"
ARCEE_NEW = "The back-face trigger counts the matching targets as it resolves (the spell's, or its last-known ones once it has left the stack; a target that left the battlefield as it last existed there), so spells with multiple controlled creature or Vehicle targets put that many counters on Arcee |"
ARCH_OLD = " Only a \"that many\" count of the spell's targets (`TriggeringSpellTargetCount`) is still counted into the effect as the ability triggers."
ARCH_NEW = " A \"that many\" count of the spell's targets (`TriggeringSpellTargetCount`) is counted as the ability resolves, from the live spell or that snapshot."


def apply(name, old, new):
    path = DOCS / name
    lines = path.read_text().split("\n")
    hits = [i for i, line in enumerate(lines) if old in line]
    if new is not None and any(line == new for line in lines):
        return False
    if not hits:
        if new is None:
            return False
        raise SystemExit(f"{name}: no line contains {old!r}")
    if len(hits) > 1:
        raise SystemExit(f"{name}: {len(hits)} lines contain {old!r}")
    if new is None:
        del lines[hits[0]]
    else:
        lines[hits[0]] = new
    path.write_text("\n".join(lines))
    return True


def replace_in_line(name, old, new):
    path = DOCS / name
    text = path.read_text()
    if new in text:
        return False
    if text.count(old) != 1:
        raise SystemExit(f"{name}: {text.count(old)} occurrences of {old!r}")
    path.write_text(text.replace(old, new))
    return True


def main():
    changed = replace_in_line("ARCHITECTURE.md", ARCH_OLD, ARCH_NEW)
    changed |= replace_in_line("ORACLE_TEXT_EFFECT_MAP.md", ARCEE_OLD, ARCEE_NEW)
    for name, old, new in EDITS:
        changed |= apply(name, old, new)
    leftovers = [f"{path.name}: {cls}" for path in DOCS.glob("*.md") for cls in REMOVED
                 if re.search(rf"\b{cls}\b", path.read_text())]
    if leftovers:
        raise SystemExit("removed classes still named:\n" + "\n".join(leftovers))
    print("changed" if changed else "nothing to change")


if __name__ == "__main__":
    sys.exit(main())
