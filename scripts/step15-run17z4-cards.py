#!/usr/bin/env python3
"""Step 15, run 17z4: the played-name and land-tap trigger-condition records are written as abilities whose existing
effects read the stack entry.

`PlayedCardNameTriggerCollectorService` read two records and two wrappers by class, judged "a card exiled with this"
or "a card with the same name as one of the exiled cards" from the event and built or unwrapped the effect that went
on the stack; `LandTapTriggerCollectorService` read three records by class, judged the tapping player and the tapped
land and put the record's `triggeredEffect()` on the stack. Each card now writes the ability:

- "whenever [a player / you] play[s] a card exiled with this" is a cast ability and a land-play ability on
  `exiledWithThis()` (`SpellTrigger.exiledWithThis()`, `LandPlayTrigger.exiledWithThis()`), with the effect the record built (Prowl, Stoic Strategist: `DrawCardEffect(1)` and
  `TransformToBackFaceEffect()`) or wrapped (Voltaic Visionary, Share the Spoils, The Matrix of Time);
- The Matrix of Time's "that card's owner loses 3 life and exiles the top card of their library" reads the triggering
  card's owner (`LoseLifeRecipient.TRIGGERING_CARD_OWNER`, `ExileTopCardOfTriggeringPlayerLibraryEffect
  .ofTriggeringCardOwner()`);
- Search the City's "whenever you play a card with the same name as one of the exiled cards" is the filter
  `CardSharesNameWithCardExiledWithSourcePredicate` on its cast and land-play abilities, and "one of those cards with
  that name" `PutCardExiledWithSourceIntoHandEffect.withTriggeringCardNameToOwnersHand()`;
- Ethereal Forager's `PutCardExiledWithSourceIntoHandEffect(null, filter, true)` loses the removed name component;
- Share the Spoils' "exile the top card of their library" is the non-targeting `LibraryScope.TRIGGERING_PLAYER`,
  where the wrapper's follow-up said `TARGET_PLAYER` and the handler set the player without a target choice;
- Manabarbs, Tember City, Scald, Burning Earth: "deals 1 damage to that player" is
  `DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)`, the land filter the trigger's `withFilter`;
- Overabundance, Barbflare Gremlin: the same damage as the second step of the triggered mana ability, Barbflare's
  `dealtByTriggeringPermanent()` ("that land deals 1 damage");
- Sanctimony: `Triggers.opponentTapsLandForMana().withFilter(Mountain)` + `MayEffect(GainLifeEffect(1), …)`;
- Vorinclex, Voice of Hunger: `Triggers.opponentTapsLandForMana()` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z4-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."

EXILED_SPELL = "exiledWithThis()"
EXILED_IMPORTS = []
DAMAGE = "new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)"
DAMAGE_IMPORTS = [E + "DealDamageToPlayersEffect", E + "DamageRecipient"]

CARDS = {
    "ProwlStoicStrategist": (
        [("""        addTrigger(Triggers.playerCastsSpell(),
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());
        addTrigger(Triggers.opponentPlaysLand(),
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());""",
          f"""        // Whenever a player plays a card exiled with Prowl, you draw a card and convert Prowl.
        addTrigger(Triggers.playerCastsSpell().{EXILED_SPELL},
                new DrawCardEffect(1), new TransformToBackFaceEffect());
        addTrigger(Triggers.youPlayLand().exiledWithThis(),
                new DrawCardEffect(1), new TransformToBackFaceEffect());
        addTrigger(Triggers.opponentPlaysLand().exiledWithThis(),
                new DrawCardEffect(1), new TransformToBackFaceEffect());""")],
        EXILED_IMPORTS + [E + "DrawCardEffect", E + "TransformToBackFaceEffect"],
        [E + "PlayedCardExiledWithSourceDrawAndTransformTriggerEffect"]),
    "VoltaicVisionary": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new PlayedCardExiledWithSourceTriggerEffect());
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardExiledWithSourceTriggerEffect());""",
          f"""        // When you play a card exiled with this creature, transform this creature.
        addTrigger(Triggers.youCastSpell().{EXILED_SPELL}, new TransformSelfEffect());
        addTrigger(Triggers.youPlayLand().exiledWithThis(), new TransformSelfEffect());""")],
        EXILED_IMPORTS + [E + "TransformSelfEffect"],
        [E + "PlayedCardExiledWithSourceTriggerEffect"]),
    "ShareTheSpoils": (
        [("""        PlayedCardExiledWithSourceTriggerEffect exileTopCard = new PlayedCardExiledWithSourceTriggerEffect(
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER));
        addTrigger(Triggers.playerCastsSpell(), exileTopCard);
        addTrigger(Triggers.youPlayLand(), exileTopCard);
        addTrigger(Triggers.opponentPlaysLand(), exileTopCard);""",
          f"""        // When they do, exile the top card of their library.
        ExileTopCardsToSourceEffect exileTopCard =
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TRIGGERING_PLAYER);
        addTrigger(Triggers.playerCastsSpell().{EXILED_SPELL}, exileTopCard);
        addTrigger(Triggers.youPlayLand().exiledWithThis(), exileTopCard);
        addTrigger(Triggers.opponentPlaysLand().exiledWithThis(), exileTopCard);""")],
        EXILED_IMPORTS,
        [E + "PlayedCardExiledWithSourceTriggerEffect"]),
    "TheMatrixOfTime": (
        [("""        CardEffect playedCardFollowUp = SequenceEffect.of(
                new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER),
                new ExileTopCardOfTriggeringPlayerLibraryEffect());
        addTrigger(Triggers.youCastSpell(),
                new PlayedCardExiledWithPlanarSourceTriggerEffect(playedCardFollowUp));
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardExiledWithPlanarSourceTriggerEffect(playedCardFollowUp));""",
          f"""        // Whenever you play a land or cast a spell from among cards exiled with The Matrix of Time, that card's
        // owner loses 3 life and exiles the top card of their library.
        CardEffect playedCardFollowUp = SequenceEffect.of(
                new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_CARD_OWNER),
                ExileTopCardOfTriggeringPlayerLibraryEffect.ofTriggeringCardOwner());
        addTrigger(Triggers.youCastSpell().{EXILED_SPELL}, playedCardFollowUp);
        addTrigger(Triggers.youPlayLand().exiledWithThis(), playedCardFollowUp);""")],
        EXILED_IMPORTS,
        [E + "PlayedCardExiledWithPlanarSourceTriggerEffect"]),
    "SearchTheCity": (
        [(""" * <p>"Play a card" is both halves of playing, so the name-match trigger sits on the spell-cast slot
 * and on the land-play slot. The collector builds the whole ability (optional return, then the
 * intervening-if sacrifice + extra turn).
 */""",
          """ * <p>"Play a card" is both halves of playing (the card's ruling), so the ability is written as a cast
 * ability and a land-play ability, each filtered to a card sharing a name with a card exiled with this
 * enchantment; "that name" is read from the stack entry's triggering card as the ability resolves.
 */"""),
         ("""        addTrigger(Triggers.youCastSpell(),
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());""",
          """        CardEffect[] playedCardWithExiledName = {
                new MayEffect(PutCardExiledWithSourceIntoHandEffect.withTriggeringCardNameToOwnersHand(),
                        "Put a card with that name exiled with Search the City into its owner's hand?"),
                new ConditionalEffect(new NoCardsExiledWithSource(),
                        new ConditionalEffect(new SourceIsOnBattlefield(),
                                SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1))))};
        addTrigger(Triggers.youCastSpell().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate()),
                playedCardWithExiledName);
        addTrigger(Triggers.youPlayLand().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate()),
                playedCardWithExiledName);""")],
        [E + "CardEffect", E + "MayEffect", E + "PutCardExiledWithSourceIntoHandEffect", E + "ConditionalEffect",
         "model.condition.NoCardsExiledWithSource", "model.condition.SourceIsOnBattlefield", E + "SequenceEffect",
         E + "SacrificeSelfEffect", E + "ControllerExtraTurnEffect",
         F + "CardSharesNameWithCardExiledWithSourcePredicate"],
        [E + "PlayedCardNameMatchesCardExiledWithSourceTriggerEffect"]),
    "EtherealForager": (
        [("""new PutCardExiledWithSourceIntoHandEffect(null, instantOrSorcery, true)""",
          """new PutCardExiledWithSourceIntoHandEffect(instantOrSorcery, true)""")],
        [], []),
    "Manabarbs": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), new DealDamageOnLandTapEffect(1));""",
          f"""        // Whenever a player taps a land for mana, Manabarbs deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana(),
                {DAMAGE});""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "TemberCity": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), new DealDamageOnLandTapEffect(1));""",
          f"""        // Whenever a player taps a land for mana, Tember City deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana(),
                {DAMAGE});""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "Scald": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(),
                new DealDamageOnLandTapEffect(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));""",
          f"""        // Whenever a player taps an Island for mana, Scald deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                {DAMAGE});""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "BurningEarth": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), new DealDamageOnLandTapEffect(1,
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));""",
          f"""        addTrigger(Triggers.playerTapsLandForMana().withFilter(
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))),
                {DAMAGE});""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "Overabundance": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), SequenceEffect.of(
                new AddOneOfEachManaTypeProducedByLandEffect(false),
                new DealDamageOnLandTapEffect(1)));""",
          f"""        // Whenever a player taps a land for mana, that player adds one mana of any type that land produced,
        // and Overabundance deals 1 damage to them.
        addTrigger(Triggers.playerTapsLandForMana(), SequenceEffect.of(
                new AddOneOfEachManaTypeProducedByLandEffect(false),
                {DAMAGE}));""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "BarbflareGremlin": (
        [("""                SequenceEffect.of(
new AddOneOfEachManaTypeProducedByLandEffect(false),
new DealDamageOnLandTapEffect(1, null, true)));""",
          f"""                SequenceEffect.of(
                        new AddOneOfEachManaTypeProducedByLandEffect(false),
                        {DAMAGE}
                                .dealtByTriggeringPermanent()));""")],
        DAMAGE_IMPORTS, [E + "DealDamageOnLandTapEffect"]),
    "Sanctimony": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(),
                new GainLifeWhenOpponentTapsLandOfSubtypeEffect(CardSubtype.MOUNTAIN, 1));""",
          """        addTrigger(Triggers.opponentTapsLandForMana().withFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));""")],
        [E + "MayEffect", E + "GainLifeEffect", F + "PermanentHasSubtypePredicate"],
        [E + "GainLifeWhenOpponentTapsLandOfSubtypeEffect"]),
    "VorinclexVoiceOfHunger": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), new OpponentTappedLandDoesntUntapEffect());""",
          """        // Whenever an opponent taps a land for mana, that land doesn't untap during its controller's next untap
        // step.
        addTrigger(Triggers.opponentTapsLandForMana(), new SkipNextUntapEffect(TapUntapScope.TRIGGERING));""")],
        [E + "SkipNextUntapEffect", E + "TapUntapScope"],
        [E + "OpponentTappedLandDoesntUntapEffect"]),
}

GONE = ["PlayedCardExiledWithSourceDrawAndTransformTriggerEffect", "PlayedCardNameMatchesCardExiledWithSourceTriggerEffect",
        "PlayedCardExiledWithSourceTriggerEffect", "PlayedCardExiledWithPlanarSourceTriggerEffect",
        "DealDamageOnLandTapEffect", "GainLifeWhenOpponentTapsLandOfSubtypeEffect",
        "OpponentTappedLandDoesntUntapEffect"]


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    line = import_line(fqcn) + "\n"
    simple = line.rsplit(".", 1)[1].rstrip(";\n")
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


def sort_imports(text):
    """Keeps the project's import block sorted: the package's imports first, then java.*."""
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    joined = "\n".join(ours) + ("\n\n" + "\n".join(java) if java and ours else "\n".join(java))
    return text[:start] + joined + text[end:]


def convert_file(path, name):
    if name not in CARDS:
        return
    text = path.read_text()
    original = text
    edits, imports, removed = CARDS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    if text == original:
        return
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed + ["model.CardSubtype"]:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    for gone in GONE:
        if re.search(r"\b" + gone + r"\b", text):
            raise SystemExit(f"{name}: still names {gone}")
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card/{module}/src/main/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
