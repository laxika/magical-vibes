#!/usr/bin/env python3
"""Step 15, run 17x4: the life, emblem, step and vote families' effects read the stack entry.

Rewrites the card classes whose trigger reached the stack only through a collector that rebuilt its effect with the
event's data bound in, so each card writes the ability that goes on the stack:

- "Whenever a player loses life, that player mills that many cards" (Mindcrank, The Master of Lake-town):
  `MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER)`.
- Relic Putrescence's "its controller gets a poison counter": `PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER`.
- Lorcan, Warlock Collector's "pay life equal to its mana value": `MayPayLifeEqualToAmountEffect` with
  `TriggeringCardManaValue`.
- Cradle of Vitality: `MayPayManaEffect("{1}{W}", PutCounterOnTargetPermanentEffect.withTargetRestriction(
  PLUS_ONE_PLUS_ONE, new EventValue(), creature), ...)`.
- Nykthos Paragon: `MayEffect(PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, new EventValue(),
  creature))`.
- "Whenever you gain life, draw that many cards" (Lich's Mastery, Marina Vendrell's Grimoire):
  `DrawCardEffect(new EventValue())`.
- The emblems: Ob Nixilis Reignited's `Triggers.playerDrawsCard()` with `LoseLifeEffect(2, CONTROLLER)`; Jace,
  Telepath Unbound's `Triggers.youCastSpell()` with `MillEffect(5, TARGET_PLAYER)` and an opponent target filter;
  Ral, Izzet Viceroy's, Narset of the Ancient Way's and Chandra, Torch of Defiance's filtered cast triggers with
  `DealDamageToAnyTargetEffect` (Ral's with `DrawCardEffect(2)`), all as `TriggeredAbility`s of the emblem.
- Curse of Oblivion's "that player exiles two cards from their graveyard": `GraveyardExileScope.TRIGGERING_PLAYER`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17x4-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

MILL = ("new MillOpponentOnLifeLossEffect()", "new MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER)")
MILL_IMPORTS = ["model.effect.MillEffect", "model.effect.MillRecipient", "model.amount.EventValue"]
DRAW = ("new DrawCardsEqualToLifeGainedEffect()", "new DrawCardEffect(new EventValue())")
DRAW_IMPORTS = ["model.effect.DrawCardEffect", "model.amount.EventValue"]
EMBLEM_IMPORTS = ["model.TriggeredAbility", "model.trigger.Triggers"]

CARDS = {
    "Mindcrank": ([MILL], MILL_IMPORTS),
    "TheMasterOfLakeTown": ([MILL], MILL_IMPORTS),
    "RelicPutrescence": ([("PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER",
                           "PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER")], []),
    "LorcanWarlockCollector": ([("""                new MayPayLifeEffect(0,
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(""",
                                 """                new MayPayLifeEqualToAmountEffect(new TriggeringCardManaValue(),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(""")],
                               ["model.effect.MayPayLifeEqualToAmountEffect", "model.amount.TriggeringCardManaValue"]),
    "CradleOfVitality": ([("""        addTrigger(Triggers.youGainLife(), new CradleOfVitalityLifeGainEffect("{1}{W}"));""",
                           """        addTrigger(Triggers.youGainLife(), new MayPayManaEffect("{1}{W}",
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.PLUS_ONE_PLUS_ONE,
                        new EventValue(), new PermanentIsCreaturePredicate()),
                "Pay {1}{W} to put a +1/+1 counter on target creature for each 1 life you gained?"));""")],
                         ["model.effect.MayPayManaEffect", "model.effect.PutCounterOnTargetPermanentEffect",
                          "model.CounterType", "model.amount.EventValue", "model.filter.PermanentIsCreaturePredicate"]),
    "NykthosParagon": ([("""                new NykthosParagonLifeGainEffect());""",
                         """                new MayEffect(new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new EventValue(), new PermanentIsCreaturePredicate()),
                        "Put that many +1/+1 counters on each creature you control?"));""")],
                       ["model.effect.MayEffect", "model.effect.PutCounterOnEachControlledPermanentEffect",
                        "model.CounterType", "model.amount.EventValue", "model.filter.PermanentIsCreaturePredicate"]),
    "LichsMastery": ([DRAW], DRAW_IMPORTS),
    "MarinaVendrellsGrimoire": ([DRAW], DRAW_IMPORTS),
    "ObNixilisReignited": ([("""                List.of(new CreateEmblemEffect(
                        List.of(new EmblemControllerLosesLifeOnAnyPlayerDrawEffect(2)),
                        EMBLEM_TEXT,
                        EmblemRecipient.TARGET_PLAYER)),""",
                             """                List.of(new CreateEmblemEffect(
                        List.of(),
                        EMBLEM_TEXT,
                        EmblemRecipient.TARGET_PLAYER,
                        List.of(),
                        List.of(new TriggeredAbility(Triggers.playerDrawsCard(),
                                new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER))))),""")], EMBLEM_IMPORTS),
    "JaceTelepathUnbound": ([("""                List.of(new CreateEmblemEffect(
                        List.of(new MillEffect(5, MillRecipient.TARGET_PLAYER)),
                        "Whenever you cast a spell, target opponent mills five cards.")),""",
                              """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell(), new MillEffect(5, MillRecipient.TARGET_PLAYER))
                                .withTargetFilter(new PlayerPredicateTargetFilter(
                                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                        "Target must be an opponent")),
                        "Whenever you cast a spell, target opponent mills five cards.")),""")],
                            EMBLEM_IMPORTS + ["model.filter.PlayerPredicateTargetFilter",
                                              "model.filter.PlayerRelationPredicate", "model.filter.PlayerRelation"]),
    "RalIzzetViceroy": ([("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(
                                4, INSTANT_OR_SORCERY, List.of(new DrawCardEffect(2)))),""",
                          """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY),
                                new DealDamageToAnyTargetEffect(4), new DrawCardEffect(2)),""")],
                        EMBLEM_IMPORTS + ["model.effect.DealDamageToAnyTargetEffect"]),
    "NarsetOfTheAncientWay": ([("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(
                                2,
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of())),""",
                                """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell()
                                .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                                new DealDamageToAnyTargetEffect(2)),""")],
                              EMBLEM_IMPORTS + ["model.effect.DealDamageToAnyTargetEffect"]),
    "ChandraTorchOfDefiance": ([("""                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerSpellCastEffect(5)),""",
                                 """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell(), new DealDamageToAnyTargetEffect(5)),""")],
                               EMBLEM_IMPORTS + ["model.effect.DealDamageToAnyTargetEffect"]),
    "CurseOfOblivion": ([("new ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN)",
                          "new ExileGraveyardCardsEffect(2, GraveyardExileScope.TRIGGERING_PLAYER)")], []),
}


def imp(cls):
    return f"import {PKG}.{cls};"


def remove_unused_imports(text):
    lines = text.split("\n")
    body = "\n".join(l for l in lines if not l.startswith("import "))
    kept = []
    for line in lines:
        match = re.match(r"import ([\w.]+)\.(\w+);$", line)
        if match and not re.search(r"\b" + match.group(2) + r"\b", body):
            continue
        kept.append(line)
    text = "\n".join(kept)
    return re.sub(r"\n\n\n+", "\n\n", text)


def add_imports(text, add):
    lines = text.split("\n")
    for cls in add:
        line = imp(cls)
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import java.")]
        position = next((i for i in imports if lines[i] > line), imports[-1] + 1)
        lines.insert(position, line)
    return "\n".join(lines)


def add_java_list(text):
    if "import java.util.List;" in text:
        return text
    lines = text.split("\n")
    java = [i for i, l in enumerate(lines) if l.startswith("import java.")]
    if java:
        position = next((i for i in java if lines[i] > "import java.util.List;"), java[-1] + 1)
        lines.insert(position, "import java.util.List;")
    else:
        last = max(i for i, l in enumerate(lines) if l.startswith("import "))
        lines[last + 1:last + 1] = ["", "import java.util.List;"]
    return "\n".join(lines)


def find_card(name, modules):
    for module in modules:
        base = ROOT / f"magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards"
        for path in base.glob(f"*/{name}.java"):
            return path
    return None


def convert(name, path, edits, add):
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    if text == original:
        return
    text = add_imports(text, add)
    if "List.of(" in text:
        text = add_java_list(text)
    text = remove_unused_imports(text)
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz") | {"shared"}) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for name, (edits, add) in CARDS.items():
        path = find_card(name, modules)
        if path is not None:
            convert(name, path, edits, add)


if __name__ == "__main__":
    main()
