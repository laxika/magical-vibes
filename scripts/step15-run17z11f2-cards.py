#!/usr/bin/env python3
"""Step 15, run 17z11f2: the "when you cast this spell" cards and Venser's emblem (idempotent, per letter module).

- The cards that wrote one "when you cast this spell" ability as several `addTrigger` calls get one call with all its
  effects: the self-cast collector now puts each ability on the stack as one object (CR 603.3), so one Oracle ability is
  one ability. A card's first such call takes the effects of the others (a chained `target(...)` call binds them all to
  its target group).
- "Spells you cast … have cascade" is `.grantedToTheSpell()` on the cast trigger (Imoti, Zhulodok, TARDIS Bay,
  Wild-Magic Sorcerer); Jodah, the Unifier's ability is its own and stays as written.
- Venser, the Sojourner's emblem is a `TriggeredAbility` on `Triggers.youCastSpell()` with `ExileTargetPermanentEffect`.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
MERGED = ["AbundantMaw", "DecimatorOfTheProvinces", "FlayerOfLoyalties", "GluttonousHellkite", "HydroidKrasis",
          "HideousTaskmaster", "KozilekCompleated", "MutatedCultist", "OblivionSower", "SauronLordOfTheRings",
          "TheBeastDeathlessPrince"]
GRANTED = {
    "ImotiCelebrantOfBounty": ("Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(6, true))",
                               "Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(6, true))"
                               ".grantedToTheSpell()"),
    "ZhulodokVoidGorger": (".withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND)),",
                           ".withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND))\n"
                           "                        .grantedToTheSpell(),"),
    "TardisBay": ("Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(2, true)).nthThisTurn(1),",
                  "Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(2, true)).nthThisTurn(1)\n"
                  "                        .grantedToTheSpell(),"),
    "WildMagicSorcerer": ("Triggers.youCastSpell().castFrom(Zone.EXILE).nthThisTurn(1), new CascadeEffect()",
                          "Triggers.youCastSpell().castFrom(Zone.EXILE).nthThisTurn(1).grantedToTheSpell(),\n"
                          "                new CascadeEffect()"),
}
CALL = "addTrigger(Triggers.youCastThisSpell(),"


def card(name):
    found = sorted(CARDS.glob(f"*/src/main/java/com/github/laxika/magicalvibes/cards/*/{name}.java"))
    if len(found) != 1:
        sys.exit(f"{name}: expected one card class, found {found}")
    return found[0]


def call_end(text, start):
    depth = 0
    for i in range(start, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("unbalanced")


def merge(text):
    starts = [m.start() for m in re.finditer(re.escape(CALL), text)]
    if len(starts) < 2:
        return text
    args = []
    spans = []
    for start in starts:
        end = call_end(text, start)
        args.append(text[start + len(CALL):end].strip())
        spans.append((start, end))
    first_start, first_end = spans[0]
    line_start = text.rfind("\n", 0, first_start) + 1
    indent = " " * (len(text[line_start:first_start]) - len(text[line_start:first_start].lstrip()) + 8)
    merged = CALL + "\n" + indent + (",\n" + indent).join(args) + ")"
    pieces = []
    cursor = 0
    for index, (start, end) in enumerate(spans):
        if index == 0:
            pieces.append(text[cursor:start])
            pieces.append(merged)
            cursor = end + 1
            continue
        # Drop the call with what joins it to the previous one: a chained ".addTrigger(...)" or a statement.
        before = text[cursor:start]
        chained = re.search(r"\s*\.\s*$", before)
        if chained:
            pieces.append(before[:chained.start()])
            cursor = end + 1
        else:
            statement = re.search(r"\n[ \t]*$", before)
            pieces.append(before[:statement.start()] if statement else before)
            after = text[end + 1:]
            cursor = end + 1 + (1 if after.startswith(";") else 0)
    pieces.append(text[cursor:])
    return "".join(pieces)


def main():
    changed = 0
    for name in MERGED:
        path = card(name)
        text = path.read_text()
        new = merge(text)
        if new != text:
            path.write_text(new)
            changed += 1
    for name, (old, new) in GRANTED.items():
        path = card(name)
        text = path.read_text()
        if "grantedToTheSpell()" in text:
            continue
        if text.count(old) != 1:
            sys.exit(f"{name}: anchor not found once")
        path.write_text(text.replace(old, new))
        changed += 1
    venser = card("VenserTheSojourner")
    text = venser.read_text()
    old = """                List.of(new CreateEmblemEffect(
                        List.of(new ExileTargetOnControllerSpellCastEffect()),
                        "Whenever you cast a spell, exile target permanent.")),"""
    new = """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell(), new ExileTargetPermanentEffect()),
                        "Whenever you cast a spell, exile target permanent.")),"""
    if old in text:
        text = text.replace(old, new)
        text = text.replace("import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;\n",
                            "")
        for imp in ("com.github.laxika.magicalvibes.model.TriggeredAbility",
                    "com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect",
                    "com.github.laxika.magicalvibes.model.trigger.Triggers"):
            if f"import {imp};" not in text:
                text = text.replace("\nimport ", f"\nimport {imp};\nimport ", 1)
        venser.write_text(text)
        changed += 1
    print(f"changed {changed} card classes")


if __name__ == "__main__":
    main()
