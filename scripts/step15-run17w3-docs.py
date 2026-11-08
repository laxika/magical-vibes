#!/usr/bin/env python3
"""Step 15, run 17w3: the agent-docs rows of the cast triggers' counter and exile rebuilds.

Rewrites the rows that named the deleted trigger records (`CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect`,
`CounterSpellIfManaValueEqualsSourceCountersEffect`, `CounterSpellIfNoColoredManaSpentEffect`,
`KnowledgePoolCastTriggerEffect`, `PossibilityStormCastTriggerEffect`, `EyeOfTheStormCastTriggerEffect`) or the old
signatures of the resolution effects that lost their bound ids, adds `StackEntryColoredManaSpentPredicate` to
`PREDICATES_REFERENCE.md` and a sentence to `ARCHITECTURE.md`'s triggering-spell paragraph, and marks the
`SpellCastTriggerEffect` record gone (in the implement-card skill too). The header notes and legacy rows that still
write a cast trigger as `SpellCastTriggerEffect(filter, effects, …)` shorthand stay, their note now saying the record
is gone. Idempotent. Fails if a doc or skill still names one of the deleted trigger records.

Usage: step15-run17w3-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

OLD_NOTE = "See `SpellCastTriggerEffect` in `EFFECTS_INDEX.md`; the record itself is built by the engine."
NEW_NOTE = "The record is gone (effect-slot-refactor.md step 15, run 17w3)."
NOTE_FILES = ["ORACLE_TEXT_EFFECT_MAP.md", "CARD_PATTERN_INDEX.md", "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md",
              "CARD_PATTERNS_CREATURES_TRIGGERED.md", "CARD_PATTERNS_PERMANENTS_STATIC.md", "CARD_PATTERNS_LANDS_SPELLS.md"]

COUNTER_CAST = ("`Triggers.playerCastsSpell()` with a filter and `CounterSpellEffect()`: the generic cast collector puts "
                "it on the stack aimed at the triggering spell, which isn't a target")

# file -> [(old line prefix, new line)]: the line starting with the prefix is replaced whole.
LINES = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect()` —",
         "- `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect()` — on `Triggers.playerCastsSpell().anySpell()`: any "
         "player other than the spell's caster may pay that spell's printed mana cost, including the chosen value of X, to "
         "counter it (Ice Cave). As the ability resolves it reads the triggering spell (the live spell, else the stack "
         "entry's snapshot) and inserts `MayPayManaEffect(cost, CounterSpellEffect(), …, MayPayPayer.ANY_OTHER_PLAYER)` "
         "as the next effect; `ANY_OTHER_PLAYER` leaves out the triggering spell's controller (effect-slot-refactor.md "
         "step 15, run 17w3)"),
        ("- `CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType)` —",
         "- \"Whenever a player casts a spell with mana value equal to the number of [counters] on this, counter that "
         "spell\" is `Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate("
         "CounterType.CHARGE))` with `CounterSpellEffect()` (Chalice of the Void): the counters matter only as the spell is "
         "cast, a trigger condition, not an intervening \"if\" (effect-slot-refactor.md step 15, run 17w3)"),
        ("- `CounterSpellIfNoColoredManaSpentEffect()` —",
         "- \"Whenever a player casts a spell, if no colored mana was spent to cast it, counter that spell\" is "
         "`Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryNotPredicate(new "
         "StackEntryColoredManaSpentPredicate()))` with `CounterSpellEffect()` (Void Mirror): what was spent can't change "
         "once the spell is cast, so the filter judges the \"if\" as it would resolve too (effect-slot-refactor.md step "
         "15, run 17w3)"),
        ("- `PossibilityStormCastTriggerEffect()` —",
         "- `PossibilityStormExileAndCastEffect()` — on `Triggers.playerCastsSpell().castFrom(Zone.HAND)`: the caster "
         "exiles the triggering spell (only while it is still on the stack), digs until a card shares a card type with it "
         "(the stack entry's snapshot of the spell once it has left), may cast it free, then all cards exiled with the "
         "enchantment go on the bottom in a random order (Possibility Storm)"),
        ("- `EyeOfTheStormCastTriggerEffect()` + `EyeOfTheStormExileAndCopyEffect(",
         "- `EyeOfTheStormExileAndCopyEffect()` — on `Triggers.playerCastsSpell().withFilter(instant or sorcery)"
         ".withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true)))` (an instant or sorcery "
         "card; a cast copy doesn't trigger it): exile the triggering spell tracked by the source, copy every instant or "
         "sorcery card exiled with that source, then let the caster choose any subset and order of the generated copies "
         "for free casting (Eye of the Storm). It reads the spell, the caster and the source from the stack entry"),
        ("- `CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect()` —",
         "- \"Counter that spell unless that player pays {X}, where X is the number of cards in all graveyards with the "
         "same name as the spell\" is `CounterUnlessPaysEffect(new CardsInGraveyard(new "
         "CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER))` on `Triggers.playerCastsSpell()"
         ".anySpell()` (Cephalid Shrine): aimed at the triggering spell without targeting it, the payment offered even to "
         "a spell that can't be countered (CR 118.12a) (effect-slot-refactor.md step 15, run 17w3)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect` |",
         "| `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect` | `()` | on `Triggers.playerCastsSpell().anySpell()`: "
         "another player may pay that spell's printed mana cost, with its chosen X, to counter it. As it resolves it reads "
         "the triggering spell from the stack entry (live, else its snapshot) and inserts a `MayPayManaEffect` with "
         "`MayPayPayer.ANY_OTHER_PLAYER` and `CounterSpellEffect` as the next effect. Used by Ice Cave |"),
        ("| `CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect` |", None),
        ("`CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType)` is an",
         "\"Counter that spell\" of a cast trigger is " + COUNTER_CAST + ": Chalice of the Void's "
         "`StackEntryManaValueEqualsSourceCountersPredicate(CHARGE)` (the counters as the spell is cast) and Void Mirror's "
         "`StackEntryNotPredicate(StackEntryColoredManaSpentPredicate)` are the stack-entry filters, and Cephalid Shrine's "
         "`CounterUnlessPaysEffect(CardsInGraveyard(CardSharesNameWithTriggeringSpellPredicate, ANY_PLAYER))` reads the "
         "spell's name as it resolves (effect-slot-refactor.md step 15, run 17w3)."),
        ("`CounterSpellIfNoColoredManaSpentEffect` is an", None),
        ("| `KnowledgePoolCastTriggerEffect` |", None),
        ("| `KnowledgePoolExileAndCastEffect` |",
         "| `KnowledgePoolExileAndCastEffect` | `()` | Knowledge Pool's cast trigger (`Triggers.playerCastsSpell()"
         ".castFrom(Zone.HAND)`, so its own free casts don't retrigger it). Exiles the triggering spell from the stack to "
         "the pool of the stack entry's source (nothing more if it has left), then presents the caster, the entry's "
         "non-targeting \"that player\" (the trigger is controlled by the KP controller per CR 603.3a), with a choice of "
         "nonland \"other\" cards from the pool to cast without paying mana cost. Uses `KNOWLEDGE_POOL_CAST_CHOICE` "
         "awaiting input |"),
        ("| `PossibilityStormCastTriggerEffect` |", None),
        ("| `PossibilityStormExileAndCastEffect` |",
         "| `PossibilityStormExileAndCastEffect` | `()` | Possibility Storm's cast trigger (`Triggers.playerCastsSpell()"
         ".castFrom(Zone.HAND)`). Exiles the triggering spell from the stack tracked with the stack entry's source, then "
         "exiles cards from the top of the caster's library until one shares a card type with it (the entry's snapshot of "
         "the spell once it has left), and queues a `MayCastExiledCardThenBottomRestEffect` pending may-ability offering "
         "that card as a free cast. If the library runs out with no match, everything is bottomed immediately. Works even "
         "if the enchantment already left the battlefield. Handler: `PossibilityStormExileAndCastEffectHandler` |"),
        ("| `EyeOfTheStormCastTriggerEffect` |", None),
        ("| `EyeOfTheStormExileAndCopyEffect` |",
         "| `EyeOfTheStormExileAndCopyEffect` | `()` | Eye of the Storm's cast trigger (`Triggers.playerCastsSpell()` "
         "filtered to instant or sorcery cards that aren't cast copies). Exiles the triggering card from the stack tracked "
         "with the entry's source, copies every instant or sorcery card exiled with that source, and opens "
         "`PendingInteraction.EyeOfTheStormCastChoice` so the caster chooses any subset and order of those copies for free "
         "casting. Handler: `EyeOfTheStormExileAndCopyEffectHandler` |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| No colored mana spell-cast counter |",
         "| No colored mana spell-cast counter | `v/VoidMirror.java` | `Triggers.playerCastsSpell().withStackEntryFilter("
         "new StackEntryNotPredicate(new StackEntryColoredManaSpentPredicate()))` + `CounterSpellEffect()` — the generic "
         "cast collector aims the counter at the triggering spell without targeting it |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("- Spell cast trigger — another player may pay the spell's mana cost to counter it:",
         "- Spell cast trigger — another player may pay the spell's mana cost to counter it: `i/IceCave.java` uses "
         "`Triggers.playerCastsSpell().anySpell()` with `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect()`, which "
         "reads the triggering spell's printed mana cost and chosen X as it resolves and inserts `MayPayManaEffect` with "
         "`MayPayPayer.ANY_OTHER_PLAYER` aimed at the spell."),
        ("| Spell cast trigger — counter unless caster pays for same-name graveyard cards |",
         "| Spell cast trigger — counter unless caster pays for same-name graveyard cards | `c/CephalidShrine.java` | "
         "`Triggers.playerCastsSpell().anySpell()` + `CounterUnlessPaysEffect(new CardsInGraveyard(new "
         "CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER))` — aimed at the triggering spell without "
         "targeting it; the count reads the spell's name as it resolves |"),
        ("| Knowledge Pool-style cast intercept | `k/KnowledgePool.java` | Triggers.enters(ObjectRelation.THIS) "
         "ExileTopCardsToSourceEffect",
         "| Knowledge Pool-style cast intercept | `k/KnowledgePool.java` | Triggers.enters(ObjectRelation.THIS) "
         "ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER) + `Triggers.playerCastsSpell()"
         ".castFrom(Zone.HAND)` `KnowledgePoolExileAndCastEffect()` — exiles cast-from-hand spells, lets the caster pick a "
         "nonland card from the pool to cast free. Uses per-permanent exile tracking (permanentExiledCards) and "
         "KNOWLEDGE_POOL_CAST_CHOICE awaiting input |"),
        ("| Knowledge Pool-style cast intercept | `k/KnowledgePool.java` | Triggers.enters(ObjectRelation.THIS) "
         "EachPlayerExilesTopCardsToSourceEffect", None),
        ("| Possibility Storm cast intercept |",
         "| Possibility Storm cast intercept | `p/PossibilityStorm.java` | `Triggers.playerCastsSpell().castFrom(Zone.HAND)` "
         "`PossibilityStormExileAndCastEffect()` — exiles the cast-from-hand spell, digs the caster's library until a card "
         "shares a card type with it, offers it as a free cast (`MayCastExiledCardThenBottomRestEffect`), then bottoms "
         "everything exiled with the enchantment in a random order |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a player casts a spell, any other player may pay that spell's mana cost.",
         "| \"Whenever a player casts a spell, any other player may pay that spell's mana cost. If a player does, counter "
         "the spell.\" | `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect()` | `Triggers.playerCastsSpell().anySpell()` "
         "| Ice Cave. As it resolves, the effect reads the triggering spell's printed mana cost, each `{X}` the chosen X "
         "(the stack entry's snapshot once the spell has left), and offers the payment to every player except the spell's "
         "caster in APNAP order |"),
        ("| \"Whenever a player casts a spell with mana value equal to the number of charge counters on this artifact,",
         "| \"Whenever a player casts a spell with mana value equal to the number of charge counters on this artifact, "
         "counter that spell\" | `CounterSpellEffect()` | `Triggers.playerCastsSpell().withStackEntryFilter(new "
         "StackEntryManaValueEqualsSourceCountersPredicate(CounterType.CHARGE))` | Chalice of the Void (MRD). A trigger "
         "condition (the ruling: the counters matter only as the spell is cast); the generic cast collector aims the "
         "counter at the triggering spell without targeting it |"),
        ("| \"Whenever a player casts a spell, if no colored mana was spent to cast it, counter that spell\" |",
         "| \"Whenever a player casts a spell, if no colored mana was spent to cast it, counter that spell\" | "
         "`CounterSpellEffect()` | `Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryNotPredicate(new "
         "StackEntryColoredManaSpentPredicate()))` | Void Mirror (MH2). What was spent can't change once the spell is "
         "cast, so the filter judges the intervening \"if\" as it would resolve too; colorless mana spent for generic "
         "costs does not prevent the trigger |"),
        ("| \"Whenever a player casts a spell from their hand, that player exiles it, then exiles cards from the top",
         "| \"Whenever a player casts a spell from their hand, that player exiles it, then exiles cards from the top of "
         "their library until they exile a card that shares a card type with it. That player may cast that card without "
         "paying its mana cost. Then they put all cards exiled with this enchantment on the bottom of their library in a "
         "random order\" | `PossibilityStormExileAndCastEffect()` | `Triggers.playerCastsSpell().castFrom(Zone.HAND)` | "
         "Possibility Storm (DGM). The effect reads the triggering spell, the caster and the source from the stack entry; "
         "the free cast is offered via a `MayCastExiledCardThenBottomRestEffect` may-ability. Compare Knowledge Pool, "
         "which pools cards across players instead |"),
        ("| \"Whenever a player casts an instant or sorcery card, exile it. Then that player copies each instant",
         "| \"Whenever a player casts an instant or sorcery card, exile it. Then that player copies each instant or "
         "sorcery card exiled with this enchantment. For each copy, the player may cast the copy without paying its mana "
         "cost.\" | `EyeOfTheStormExileAndCopyEffect()` | `Triggers.playerCastsSpell().withFilter(instant or sorcery)"
         ".withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true)))` | Eye of the Storm (RAV "
         "48). A cast copy isn't a card, so it doesn't trigger it; resolution tracks the original card with the "
         "enchantment, creates one copy for each source-tracked instant or sorcery card, and uses "
         "`PendingInteraction.EyeOfTheStormCastChoice` so the caster chooses the subset and order of copies before the "
         "shared free-cast queue casts them. |"),
        ("| \"Whenever a player casts a spell, counter that spell unless that player pays {X}, where X is the number",
         "| \"Whenever a player casts a spell, counter that spell unless that player pays {X}, where X is the number of "
         "cards in all graveyards with the same name as the spell\" | `CounterUnlessPaysEffect(new CardsInGraveyard(new "
         "CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER))` | `Triggers.playerCastsSpell().anySpell()` "
         "| Cephalid Shrine (ODY). Aimed at the triggering spell without targeting it; the count reads the spell's name as "
         "the ability resolves, and a spell that can't be countered is still offered the payment (CR 118.12a) |"),
    ],
}

PREDICATE_ANCHOR = "| `StackEntryManaSpentLessThanManaValuePredicate` |"
PREDICATE_ROW = ("| `StackEntryColoredManaSpentPredicate` | `()` | spells that had colored mana spent to cast them (the colors "
                 "recorded as the spell was cast; read by `TargetLegalityService`). `new StackEntryNotPredicate(...)` as a "
                 "cast trigger's `.withStackEntryFilter(...)` is \"if no colored mana was spent to cast it\" (Void Mirror) |")

GONE_RECORD = [
    ("EFFECTS_QUICK_REFERENCE.md", "- `SpellCastTriggerEffect` — **Don't author** (effect-slot-refactor.md step 14, run 6b)",
     "- `SpellCastTriggerEffect` — **Gone** (effect-slot-refactor.md step 15, run 17w3; not authored since step 14, run 6b)"),
    ("EFFECTS_INDEX.md", "| `SpellCastTriggerEffect` | (built by the engine) | **Don't author** (effect-slot-refactor.md step 14, run 6b)",
     "| `SpellCastTriggerEffect` | (gone) | **Gone** (effect-slot-refactor.md step 15, run 17w3; not authored since step 14, run 6b)"),
]

ARCH_ANCHOR = ("\"the same name as that spell\" is `CardSharesNameWithTriggeringSpellPredicate`, which the "
               "`CardsInGraveyard` count reads from the entry's triggering spell.")
ARCH_ADDED = (" \"Counter that spell\" is `CounterSpellEffect` (or `CounterUnlessPaysEffect`) on a filtered cast trigger, "
              "which the generic collector aims at the triggering spell on a non-targeting entry (Chalice of the Void's "
              "and Void Mirror's conditions stack-entry filters, Cephalid Shrine's count read as it resolves); an effect "
              "that exiles \"it\" or has \"that player\" choose (Knowledge Pool, Possibility Storm, Eye of the Storm, "
              "Ice Cave's payment) reads the spell, the caster and the source permanent from the entry as it resolves.")

MISC = [
    ("agent-docs/EFFECTS_INDEX.md", "may also be the sole resolved effect of a `SpellCastTriggerEffect`.",
     "may also be the sole effect of a filtered cast trigger."),
    (".claude/skills/implement-card/SKILL.md", "is the cast trigger's filter, never a `SpellCastTriggerEffect`:",
     "is the cast trigger's filter, never a wrapper effect:"),
]

DELETED = ["CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect",
           "CounterSpellIfManaValueEqualsSourceCountersEffect", "CounterSpellIfNoColoredManaSpentEffect",
           "KnowledgePoolCastTriggerEffect", "PossibilityStormCastTriggerEffect", "EyeOfTheStormCastTriggerEffect"]


def rewrite_lines(path, edits):
    lines = path.read_text().split("\n")
    for prefix, new in edits:
        hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if new is not None and new in lines:
            continue
        if new is None and not hits:
            continue
        if len(hits) != 1:
            raise SystemExit(f"{path.name}: {len(hits)} lines start with {prefix!r}")
        if new is None:
            del lines[hits[0]]
        else:
            lines[hits[0]] = new
    path.write_text("\n".join(lines))


def main():
    for name in NOTE_FILES:
        path = DOCS / name
        path.write_text(path.read_text().replace(OLD_NOTE, NEW_NOTE))
    for name, edits in LINES.items():
        rewrite_lines(DOCS / name, edits)
    for name, old, new in GONE_RECORD:
        path = DOCS / name
        path.write_text(path.read_text().replace(old, new))
    for name, old, new in MISC:
        path = ROOT / name
        path.write_text(path.read_text().replace(old, new))
    path = DOCS / "ARCHITECTURE.md"
    text = path.read_text()
    if ARCH_ADDED not in text:
        if text.count(ARCH_ANCHOR) != 1:
            raise SystemExit("ARCHITECTURE.md: the triggering-spell paragraph's last sentence not found once")
        path.write_text(text.replace(ARCH_ANCHOR, ARCH_ANCHOR + ARCH_ADDED))
    path = DOCS / "PREDICATES_REFERENCE.md"
    text = path.read_text()
    if PREDICATE_ROW not in text:
        lines = text.split("\n")
        index = next(i for i, line in enumerate(lines) if line.startswith(PREDICATE_ANCHOR))
        lines.insert(index + 1, PREDICATE_ROW)
        path.write_text("\n".join(lines))
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = path.read_text()
        for cls in DELETED:
            if re.search(r"\b" + cls + r"\b", text):
                raise SystemExit(f"{path.relative_to(ROOT)} still names {cls}")


if __name__ == "__main__":
    main()
