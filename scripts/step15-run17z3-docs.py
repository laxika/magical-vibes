#!/usr/bin/env python3
"""Step 15, run 17z3: the agent-docs lines that name the removed attack and enlist records
(`BoostAttackingCreatureOnAttacksYouEffect`, `ScryWhenThisCreatureEnlistsEffect`,
`ConjureDuplicateOfEnlistedNontokenCreatureEffect`, `AradeshTheFounderEffect`) and the mana-value binding interface
(`TriggeringPermanentManaValueEffect`), the new `Triggers.enlists()`, `TriggeringPermanentManaValue`,
`PermanentEnlistedCreatureThisCombatPredicate`, `PutCardToBattlefieldEffect.boundedBy(DynamicAmount)` and the emblem
attack reader.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z3-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z3"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
CI = "agent-docs/CARD_PATTERN_INDEX.md"
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"
PR = "agent-docs/PREDICATES_REFERENCE.md"
CW = "agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md"

GARRUK = ("`CreateEmblemEffect(List.of(), \"…\", EmblemRecipient.TARGET_PLAYER, List.of(), List.of(new "
          "TriggeredAbility(Triggers.creatureAttacksYou(), new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, "
          "new Fixed(5), new Fixed(5), Set.of(Keyword.TRAMPLE)))))`")
EMBLEM_READER = ("`TriggerCollectionService.checkEmblemAttackTriggers` puts an emblem's \"whenever a creature attacks "
                 "you\" ability on the stack once for each creature attacking the emblem's controller directly (not a "
                 "planeswalker or battle), the attacking creature as its triggering permanent")
ARADESH = ("`Triggers.creatureYouControlAttacks()` with `TriggerOptions.DEFAULT.withInterveningIf(new "
           "TriggeringPermanentMatches(new PermanentEnlistedCreatureThisCombatPredicate()))` + "
           "`GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT)` + `ConditionalEffect(new "
           "TriggeringPermanentPowerAtLeast(4), new DrawCardEffect(1))`")
ARADESH_NOTE = ("an attack trigger with an intervening \"if\" (CR 603.4): it triggers as the creature is declared an "
                "attacker if it enlisted a creature (`GameData.creaturesThatEnlistedThisCombat`), and goes on the stack "
                "with the enlist trigger, its controller ordering the two (CR 603.3b); an effect that makes an ability "
                "of a creature attacking trigger again (Isshin) doubles it")
ENLIST = ("`Triggers.enlists()` (\"whenever this creature enlists a creature\", CR 702.154c; `.withFilter(p)` judges "
          "the enlisted creature, which is the entry's triggering permanent)")

EDITS = [
    (OM, "| \"Whenever Goblin Morale Sergeant enlists a nontoken creature,",
     "| \"Whenever Goblin Morale Sergeant enlists a nontoken creature, you may conjure a duplicate of that creature "
     "into the top five cards of your library at random. The duplicate perpetually gets +1/+0 and gains haste.\" | "
     "`MayEffect(new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(), prompt)` | "
     "`Triggers.enlists().withFilter(new PermanentNotPredicate(new PermanentIsTokenPredicate()))` | Goblin Morale "
     "Sergeant (YDMU 14); the enlisted creature is the entry's triggering permanent, read as the ability resolves "
     "(else as it last existed), and the duplicate is inserted at a random position among the top five while its "
     "perpetual +1/+0 is stored by card identity. |", "line"),
    (OM, "| \"Target opponent gets an emblem with \\\"Whenever a creature attacks you, it gets +5/+5",
     "| \"Target opponent gets an emblem with \\\"Whenever a creature attacks you, it gets +5/+5 and gains trample "
     "until end of turn.\\\"\" | " + GARRUK + " + `PlayerPredicateTargetFilter(OPPONENT)` | loyalty ability | Garruk, "
     "Apex Predator −8. The emblem is created under the **target opponent's** control; " + EMBLEM_READER + " |",
     "line"),
    (OM, "| \"Whenever this creature enlists a creature, scry 2.\" |",
     "| \"Whenever this creature enlists a creature, scry 2.\" | `ScryEffect(2)` | `Triggers.enlists()` | Guardian of "
     "New Benalia; triggers as its controller taps a creature to pay its enlist cost |", "line"),
    (OM, "| \"Whenever a creature you control attacks, if it enlisted a creature this combat,",
     "| \"Whenever a creature you control attacks, if it enlisted a creature this combat, the creature that attacked "
     "gains double strike until end of turn. If that creature's power is 4 or greater, draw a card.\" | " + ARADESH
     + " | `Triggers.creatureYouControlAttacks()` | Aradesh, the Founder: " + ARADESH_NOTE + " |", "line"),
    (OM, "| \"Whenever this creature enlists a creature, you get a one-time boon",
     "| \"Whenever this creature enlists a creature, you get a one-time boon with \\\"When you cast a creature spell, "
     "that creature enters with a +1/+1 counter on it.\\\"\" | `GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect()` "
     "| `Triggers.enlists()` | Resolution registers a persistent one-shot boon consumed by the controller's next "
     "creature spell, including across turns (Benalish Knight-Counselor) |", "line"),
    (OM, "| \"Whenever another permanent you control enters, if it wasn't put onto the battlefield with this ability, ...\" |", "",
     "fragment:Kodama of the East Tree; `PutCardToBattlefieldEffect` records the source on the permanent it puts |"
     "=>Kodama of the East Tree; `PutCardToBattlefieldEffect` records the source on the permanent it puts; \"with "
     "equal or lesser mana value\" is `.boundedBy(new TriggeringPermanentManaValue())`, read as the ability resolves |"),
    (OM, "| \"Whenever a creature you control attacks or enters attacking, you may pay {1}{R}.", "",
     "fragment:The triggering creature's mana value is snapshotted when either trigger is collected;"
     "=>The triggering creature's mana value is read as the ability resolves, else as it last existed on the "
     "battlefield (CR 608.2h);"),
    (OM, "| \"At the beginning of your end step, exile the top card of each opponent's library.", "",
     "fragment:the ETB trigger snapshots the entering permanent's mana value and makes its owner lose that much life."
     "=>the ETB trigger makes the entering permanent's owner lose life equal to its mana value, read as the ability "
     "resolves, else as it last existed on the battlefield."),
    (QR, "- `PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect()` —",
     "- `PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect()` — resolution effect for Arni Metalbrow: "
     "you may put a creature card from hand onto the battlefield tapped and attacking when its mana value is less than "
     "the triggering creature's mana value (`TriggeringPermanentManaValue`, read as it resolves)", "line"),
    (QR, "- `AradeshTheFounderEffect()` —",
     "- `AradeshTheFounderEffect` — **Gone** (" + RUN + "): Aradesh, the Founder is " + ARADESH + "; " + ARADESH_NOTE
     + ".", "line"),
    (QR, "- `ScryWhenThisCreatureEnlistsEffect()` —",
     "- `ScryWhenThisCreatureEnlistsEffect` — **Gone** (" + RUN + "): Guardian of New Benalia is `Triggers.enlists()` + "
     "`ScryEffect(2)`; " + ENLIST + ".", "line"),
    (QR, "- `ScryWhenThisCreatureEnlistsEffect` — **Gone**",
     "- `TriggeringPermanentManaValueEffect` — **Gone** (" + RUN + "): the binding interface whose effects had the "
     "triggering permanent's mana value baked into the entry's event value as the ability triggered. "
     "`new TriggeringPermanentManaValue()` (a `DynamicAmount`) reads it as the ability resolves, else as the permanent "
     "last existed on the battlefield (CR 608.2h): Junkyard Scrapper's `ManaValueBound(new TriggeringPermanentManaValue(), "
     "false, -1)`, Kodama of the East Tree's `PutCardToBattlefieldEffect(...).boundedBy(new "
     "TriggeringPermanentManaValue())`, and Arni Metalbrow's and Brainstealer Dragon's effects.", "after"),
    (QR, "- `LoseLifeToOwnerOfTriggeringPermanentEffect()` —",
     "- `LoseLifeToOwnerOfTriggeringPermanentEffect()` — enter-the-battlefield trigger payload that makes the owner of "
     "the entering permanent lose life equal to its mana value, both read as the ability resolves, else as it last "
     "existed on the battlefield (Brainstealer Dragon)", "line"),
    (QR, "- `ConjureDuplicateOfEnlistedNontokenCreatureEffect()` —",
     "- `ConjureDuplicateOfEnlistedNontokenCreatureEffect` — **Gone** (" + RUN + "): Goblin Morale Sergeant is "
     "`Triggers.enlists().withFilter(new PermanentNotPredicate(new PermanentIsTokenPredicate()))` + `MayEffect(new "
     "ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(), prompt)`; " + ENLIST + ".", "line"),
    (QR, "- `PutCardToBattlefieldEffect(...).returningToHandAtEndStep()` —",
     "- `PutCardToBattlefieldEffect(...).boundedBy(DynamicAmount)` / `.boundedByEventValue()` — only a card with mana "
     "value at most the amount, evaluated as the effect resolves, is eligible: `boundedBy(new "
     "TriggeringPermanentManaValue())` for \"with equal or lesser mana value\" (Kodama of the East Tree), "
     "`boundedByEventValue()` for \"less than or equal to that damage / that creature's power\" (Broodcaller Scourge, "
     "Covert Technician, Minn, Wily Illusionist)", "after"),
    (QR, "- `MayPayLifeEqualToAmountEffect(new TriggeringCardManaValue(), wrapped, prompt)` —",
     "- `new TriggeringPermanentManaValue()` — the mana value of the stack entry's triggering permanent, as it is when "
     "the ability resolves, else as it last existed on the battlefield (CR 608.2h): \"with lesser mana value\" "
     "(Junkyard Scrapper's `ManaValueBound`), \"with equal or lesser mana value\" (Kodama of the East Tree)", "after"),
    (IX, "| `ConjureDuplicateOfEnlistedNontokenCreatureEffect` |",
     "| `ConjureDuplicateOfEnlistedNontokenCreatureEffect` | — | **Gone** (" + RUN + "): Goblin Morale Sergeant writes "
     "`Triggers.enlists().withFilter(nontoken)` + `MayEffect(new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(), "
     "prompt)` |", "line"),
    (IX, "| `AradeshTheFounderEffect` |",
     "| `AradeshTheFounderEffect` | — | **Gone** (" + RUN + "): Aradesh, the Founder writes " + ARADESH + " |", "line"),
    (IX, "| `ScryWhenThisCreatureEnlistsEffect` |",
     "| `ScryWhenThisCreatureEnlistsEffect` | — | **Gone** (" + RUN + "): Guardian of New Benalia writes "
     "`Triggers.enlists()` + `ScryEffect(2)` |", "line"),
    (IX, "| `LoseLifeToOwnerOfTriggeringPermanentEffect` |",
     "| `LoseLifeToOwnerOfTriggeringPermanentEffect` | `()` | makes the owner of the nonland opponent-owned permanent "
     "that caused an enter-the-battlefield trigger lose life equal to its mana value, read as the ability resolves, "
     "else as it last existed on the battlefield. Used by Brainstealer Dragon |", "line"),
    (IX, "| `PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect` |",
     "| `PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect` | `()` | trigger resolution effect: you may "
     "put a creature card from hand onto the battlefield tapped and attacking when its mana value is less than the "
     "triggering permanent's mana value, read as the ability resolves (`TriggeringPermanentManaValue`). Used by Arni "
     "Metalbrow |", "line"),
    (IX, "| `BoostAttackingCreatureOnAttacksYouEffect` |",
     "| `BoostAttackingCreatureOnAttacksYouEffect` | — | **Gone** (" + RUN + "): Garruk, Apex Predator's emblem is "
     + GARRUK + "; " + EMBLEM_READER + " |", "line"),
    (IX, "| `CreateEmblemEffect` |", "",
     "fragment:`StaticBoostEffect`, `CombatAttackService` for `BoostAttackingCreatureOnAttacksYouEffect`, "
     "`TriggerCollectionService`=>`StaticBoostEffect`, `TriggerCollectionService` (with the emblem attack reader for "
     "\"whenever a creature attacks you\")"),
    (CI, "| optional enlist trigger that conjures a perpetual duplicate into the top five |",
     "| optional enlist trigger that conjures a perpetual duplicate into the top five | `Triggers.enlists().withFilter(…)` "
     "+ `MayEffect(ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect())` |", "line"),
    (CW, "| Planeswalker with planeswalker removal + token + drain removal + opponent-side emblem |", "",
     "fragment:the emblem lands under the *opponent*'s control and carries `BoostAttackingCreatureOnAttacksYouEffect(5, 5, {TRAMPLE})` |"
     "=>the emblem lands under the *opponent*'s control and carries the triggered ability `Triggers.creatureAttacksYou()` + "
     "`BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, new Fixed(5), new Fixed(5), Set.of(TRAMPLE))` |"),
    (AG, "| `Triggers.creatureAttacksYou()` |",
     "| `Triggers.enlists()` | Whenever this creature enlists a creature: as it attacks, its controller taps a creature "
     "to pay its enlist cost (CR 702.154c). `.withFilter(p)` judges the enlisted creature (\"a nontoken creature\"), "
     "the entry's triggering permanent. Used by Guardian of New Benalia, Goblin Morale Sergeant, Benalish "
     "Knight-Counselor |", "after"),
    (AG, "| `Triggers.creatureAttacksYou()` |", "",
     "fragment:Used by Barbed Foliage |=>Used by Barbed Foliage. On an emblem's `TriggeredAbility` (Garruk, Apex "
     "Predator) it is read by `TriggerCollectionService.checkEmblemAttackTriggers`, the attacker the entry's triggering "
     "permanent |"),
    (TS, "| `Triggers.creatureAttacksYou()` |",
     "| `Triggers.enlists()` | `CombatAttackService.collectEnlistAbilities` (the enlisting attacker's abilities, as its "
     "enlist cost is paid; the enlisted creature is the entry's triggering permanent) | none (no card targets) |",
     "after"),
    (PR, "| `PermanentAttackedThisTurnPredicate` |",
     "| `PermanentEnlistedCreatureThisCombatPredicate` | `()` | the creature enlisted a creature this combat (its enlist "
     "cost was paid, CR 702.154c; `GameData.creaturesThatEnlistedThisCombat`). Aradesh, the Founder's intervening "
     "\"if\" `TriggeringPermanentMatches(new PermanentEnlistedCreatureThisCombatPredicate())`; stays true for a "
     "creature that left, judged as it last existed |", "after"),
]

GONE = ["BoostAttackingCreatureOnAttacksYouEffect", "ScryWhenThisCreatureEnlistsEffect",
        "ConjureDuplicateOfEnlistedNontokenCreatureEffect", "AradeshTheFounderEffect",
        "TriggeringPermanentManaValueEffect"]


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
