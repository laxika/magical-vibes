#!/usr/bin/env python3
"""Step 15, run 17z16b2: the cards whose triggered ability read the event's value as X read the event.

A triggered ability whose target is chosen as it goes on the stack (CR 603.3d) no longer gets the event's value as
the X of its stack entry: the entry has the X the entry of an ability that chooses nothing has. The cards that read
that X read the event instead, as the target is chosen (from the trigger record) and as the ability resolves (from
the entry). `scripts/step15-run17z16b2-scan.py` lists them; the X of a spell, a cost or "enters with X counters"
(CR 107.3) is not touched.

Oracle text checked with the Scryfall MCP:

"That much damage" (`XValue` -> `EventValue`, the damage the collector records as the entry's event value):

- Boros Reckoner (GTC 215), Spitemare (EVE 147), Ill-Tempered Loner (VOW 162): "Whenever this creature is dealt
  damage, it deals that much damage to any target."
- Howlpack Avenger (VOW 162, back face): "Whenever a permanent you control is dealt damage, this creature deals that
  much damage to any target."
- Truefire Captain (GRN 209): "Whenever this creature is dealt damage, it deals that much damage to target player."
- Mogg Maniac (STH 94): "Whenever this creature is dealt damage, it deals that much damage to target opponent or
  planeswalker."
- Wall of Souls (STH 75): "Whenever this creature is dealt combat damage, it deals that much damage to target opponent
  or planeswalker."
- Spiteful Sliver (MH1 148): Sliver creatures you control have "Whenever this creature is dealt damage, it deals that
  much damage to target player or planeswalker."
- Chandra's Incinerator (M21 136): "Whenever a source you control deals noncombat damage to an opponent, this
  creature deals that much damage to target creature or planeswalker that player controls."
- Toralf, God of Fury (KHM 154): "Whenever a creature or planeswalker an opponent controls is dealt excess noncombat
  damage, Toralf deals damage equal to the excess to any target other than that permanent."

A graveyard target restricted by the event:

- Venerable Warsinger (STX 246): "you may return target creature card with mana value X or less from your graveyard to
  the battlefield, where X is the amount of damage this creature dealt to that player." The ability defines X as the
  damage (CR 107.3), the entry's event value: `requiresManaValueAtMostX` -> `dynamicMaxManaValue(new EventValue())`.
- Riveteers Ascendancy (SNC 216): "Whenever you sacrifice a creature, you may return target creature card with lesser
  mana value from your graveyard to the battlefield tapped." The sacrificed creature as it last existed (CR 603.10a)
  is the entry's triggering permanent: `CardManaValueLessThanXPredicate` ->
  `dynamicMaxManaValue(new Sum(new TriggeringCardManaValue(), new Fixed(-1)))`, as Death's Oasis writes its
  "lesser mana value" than the creature that died.
- Krile Baldesion (FIC 86): "you may return target creature card with mana value equal to that spell's mana value from
  your graveyard to your hand." The cast collector records the spell's mana value as the entry's event value:
  `requiresManaValueEqualsX` -> `CardManaValueEqualsEventValuePredicate` in the filter.
- Soundwave, Sonic Spy (BOT 14): "exile target instant or sorcery card with mana value equal to the damage dealt from
  their graveyard." `CardManaValueEqualsXPredicate` -> `CardManaValueEqualsEventValuePredicate`.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16b2-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

X_IMPORT = "import com.github.laxika.magicalvibes.model.amount.XValue;\n"
EVENT_IMPORT = "import com.github.laxika.magicalvibes.model.amount.EventValue;\n"


def that_much(effect):
    """`new <effect>(new XValue()` -> `new <effect>(new EventValue()`, with the import."""
    return [(f"new {effect}(new XValue()", f"new {effect}(new EventValue()"), (X_IMPORT, EVENT_IMPORT)]


# name -> (module, edits); the module is the letter unless the class lives in `shared`.
EDITS = {
    "BorosReckoner": ("b", that_much("DealDamageToAnyTargetEffect") + [
        ("        // The damage amount snapshots into xValue; the controller chooses the target when serviced.\n",
         "        // The damage is the entry's event value; the target is chosen as the ability goes on the stack.\n"),
    ]),
    "Spitemare": ("s", that_much("DealDamageToAnyTargetEffect") + [
        ("        // The damage amount snapshots into xValue; the controller chooses the target when serviced.\n",
         "        // The damage is the entry's event value; the target is chosen as the ability goes on the stack.\n"),
    ]),
    "IllTemperedLoner": ("i", that_much("DealDamageToAnyTargetEffect")),
    "HowlpackAvenger": ("shared", that_much("DealDamageToAnyTargetEffect")),
    "TruefireCaptain": ("t", that_much("DealDamageToTargetPlayerOrPlaneswalkerEffect")),
    "MoggManiac": ("m", that_much("DealDamageToTargetPlayerOrPlaneswalkerEffect")),
    "WallOfSouls": ("w", that_much("DealDamageToTargetPlayerOrPlaneswalkerEffect")),
    "SpitefulSliver": ("s", that_much("DealDamageToTargetPlayerOrPlaneswalkerEffect")),
    "ToralfGodOfFury": ("t", that_much("DealDamageToAnyTargetEffect")),
    "ChandrasIncinerator": ("c", [
        ("new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue())",
         "new DealDamageToTargetCreatureOrPlaneswalkerEffect(new EventValue())"),
        ("import com.github.laxika.magicalvibes.model.amount.NoncombatDamageDealtToOpponentsThisTurn;\n" + X_IMPORT,
         EVENT_IMPORT + "import com.github.laxika.magicalvibes.model.amount.NoncombatDamageDealtToOpponentsThisTurn;\n"),
    ]),
    "VenerableWarsinger": ("v", [
        ("                .requiresManaValueAtMostX(true)\n", "                .dynamicMaxManaValue(new EventValue())\n"),
        ("import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;\n",
         "import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;\n" + EVENT_IMPORT),
    ]),
    "RiveteersAscendancy": ("r", [
        ("""                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardManaValueLessThanXPredicate())))
""",
         """                .filter(new CardTypePredicate(CardType.CREATURE))
                .dynamicMaxManaValue(new Sum(new TriggeringCardManaValue(), new Fixed(-1)))
"""),
        ("import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;\n",
         "import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;\n"
         "import com.github.laxika.magicalvibes.model.amount.Fixed;\n"
         "import com.github.laxika.magicalvibes.model.amount.Sum;\n"
         "import com.github.laxika.magicalvibes.model.amount.TriggeringCardManaValue;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanXPredicate;\n", ""),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
    ]),
    "KrileBaldesion": ("k", [
        ("""                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .requiresManaValueEqualsX(true)
""",
         """                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardManaValueEqualsEventValuePredicate())))
                                .targetGraveyard(true)
"""),
        ("import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsEventValuePredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
    ]),
    "SoundwaveSonicSpy": ("s", [
        ("new CardAllOfPredicate(List.of(instantOrSorcery, new CardManaValueEqualsXPredicate()))",
         "new CardAllOfPredicate(List.of(instantOrSorcery,\n"
         "                                new CardManaValueEqualsEventValuePredicate()))"),
        ("import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsEventValuePredicate;\n"),
    ]),
}


def main():
    letters = {arg.lower() for arg in sys.argv[1:]}
    changed = 0
    for name, (module, edits) in sorted(EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(module=module, letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in edits:
            if new and new in text:
                continue
            if old in text:
                text = text.replace(old, new, 1)
            elif new:
                sys.exit(f"{name}: edit does not apply:\n{old}")
        if text != original:
            path.write_text(text)
            changed += 1
            print(f"converted {path.relative_to(ROOT)}")
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
