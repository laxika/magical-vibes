#!/usr/bin/env python3
"""Step 15, run 17z13e9: the permanent-damage cards write what the deleted class-keyed handlers judged.

- Archfiend of Spite: "whenever a source an opponent controls deals damage to this creature" is its event condition,
  "that player" (the source's controller as it dealt the damage) isn't you: `NotCondition(TargetPlayerIsController())`
  (the `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect` handler judged it, the combat reader not at all).
- Taii Wakeen, Perfect Shot: "equal to that creature's toughness" is its event condition
  `TriggeringPermanentToughnessEqualsEventValue` (its `DrawCardEffect` handler judged it).
- Death Pits of Rath ("destroy it") and Rite of Passage ("put a +1/+1 counter on it"): "it" is the damaged creature, the
  trigger's triggering permanent, not a target: `DestroyReferencedPermanentEffect(TRIGGERING, true)` and
  `PutCounterOnReferencedPermanentEffect(TRIGGERING, PLUS_ONE_PLUS_ONE)` (the target effects were read as non-targeting
  through the "a creature is dealt damage" default's `MayEffect` branch; the default now routes every targeted effect to
  its target choice).

Run per letter module: `step15-run17z13e9-cards.py a d r t`. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
EDITS = {
    "a": {"ArchfiendOfSpite": [
        ("addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,\n",
         "addTrigger(Triggers.dealtDamage(),\n"
         "                TriggerOptions.EACH_SOURCE.withEventCondition(new NotCondition(new TargetPlayerIsController())),\n"),
        ("import com.github.laxika.magicalvibes.model.MadnessCast;\n",
         "import com.github.laxika.magicalvibes.model.MadnessCast;\n"
         "import com.github.laxika.magicalvibes.model.condition.NotCondition;\n"
         "import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsController;\n"),
    ]},
    "t": {"TaiiWakeenPerfectShot": [
        ("addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToCreature(), new DrawCardEffect(1));",
         "addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToCreature(),\n"
         "                TriggerOptions.DEFAULT.withEventCondition(new TriggeringPermanentToughnessEqualsEventValue()),\n"
         "                new DrawCardEffect(1));"),
        ("import com.github.laxika.magicalvibes.model.Card;\n",
         "import com.github.laxika.magicalvibes.model.Card;\n"
         "import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentToughnessEqualsEventValue;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
    ]},
    "d": {"DeathPitsOfRath": [
        ("new DestroyTargetPermanentEffect(true));",
         "new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true));"),
        ("        // The trigger fires for every damaged creature and queues this destroy against it.\n",
         "        // \"It\" is the damaged creature, the ability's triggering permanent (no target).\n"),
        ("import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.PermanentReference;\n"),
    ]},
    "r": {"RiteOfPassage": [
        ("new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));",
         "new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));"),
        ("import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.PermanentReference;\n"
         "import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;\n"),
    ]},
}


def main(letters):
    for letter in letters or sorted(EDITS):
        for name, edits in EDITS.get(letter, {}).items():
            path = next(CARDS.glob(f"*/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"))
            text = path.read_text()
            changed = 0
            for old, new in edits:
                if new in text:
                    continue
                assert text.count(old) == 1, (name, old)
                text = text.replace(old, new)
                changed += 1
            path.write_text(text)
            print(f"{name}: {changed} edits")


if __name__ == "__main__":
    main(sys.argv[1:])
