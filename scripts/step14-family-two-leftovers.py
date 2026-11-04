#!/usr/bin/env python3
"""EffectSlot refactor step 14, run 16d: the last family 2 wrappers the cards author become trigger options.

- Tyvar the Bellicose's and Folk Hero's granted "This ability triggers only once each turn" was a
  ``OncePerTurnTriggerEffect.perGrant`` wrapper; it is the ability's ``TriggerLimit.ONCE_EACH_TURN`` now, which the
  grant keeps per granting permanent (``TriggeredAbility.instance()``). Folk Hero's authored ``SpellCastTriggerEffect``
  becomes the cast trigger's spell filter, so the spell-cast collector reads the ability itself.
- The Fourth Doctor's two once-each-turn wrappers around one Food binding are one ability of both trigger conditions
  ("play a historic land or cast a historic spell from the top of your library") with the limit.
- Apex Devastator's four cascade abilities drop ``TriggerMode.INDEPENDENT``: each is an ability of its own anyway.

Exact replacements; running it again changes nothing. Run from the repository root.
"""

import pathlib
import sys

CARDS = pathlib.Path("magical-vibes-card")

EDITS = {
    "a/src/main/java/com/github/laxika/magicalvibes/cards/a/ApexDevastator.java": [
        ("import com.github.laxika.magicalvibes.model.TriggerMode;\n", ""),
        ("addTrigger(Triggers.youCastThisSpell(), TriggerMode.INDEPENDENT, new CascadeEffect());",
         "addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());"),
    ],
    "t/src/main/java/com/github/laxika/magicalvibes/cards/t/TyvarTheBellicose.java": [
        ("import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;\n", ""),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
        ("""                new TriggeredAbility(Triggers.thisManaAbilityResolves(),
                OncePerTurnTriggerEffect.perGrant(new PutCountersOnSelfEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue()))),""",
         """                new TriggeredAbility(Triggers.thisManaAbilityResolves(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),"""),
    ],
    "f/src/main/java/com/github/laxika/magicalvibes/cards/f/FolkHero.java": [
        ("import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;\n", ""),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
        ("""                new TriggeredAbility(Triggers.youCastSpell(),
                OncePerTurnTriggerEffect.perGrant(new SpellCastTriggerEffect(
                        new CardSharesCreatureTypeWithSourcePredicate(),
                        List.of(new DrawCardEffect(1))))),""",
         """                new TriggeredAbility(
                        Triggers.youCastSpell().withFilter(new CardSharesCreatureTypeWithSourcePredicate()),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                        new DrawCardEffect(1)),"""),
    ],
    "t/src/main/java/com/github/laxika/magicalvibes/cards/t/TheFourthDoctor.java": [
        ("import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;\n", ""),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\n"
         "import java.util.List;\n"),
        ("""        CreateFoodWhenPlayingCardFromTopOfLibraryEffect createFood =
                new CreateFoodWhenPlayingCardFromTopOfLibraryEffect(historic);
        addTrigger(Triggers.youCastSpell(),
                new OncePerTurnTriggerEffect(createFood));
        addTrigger(Triggers.youPlayLand(),
                new OncePerTurnTriggerEffect(createFood));""",
         """        addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateFoodWhenPlayingCardFromTopOfLibraryEffect(historic));"""),
    ],
}


def main():
    changed = 0
    for relative, edits in EDITS.items():
        path = CARDS / relative
        text = path.read_text()
        original = text
        for old, new in edits:
            # The new text is checked first: an import edit's old line is part of its new text. A deletion
            # (an empty new text) is done when its old text is gone.
            if new and new in text or not new and old not in text:
                continue
            if old not in text:
                sys.exit(f"{path}: neither the old nor the new text of an edit is there:\n{old}")
            text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            changed += 1
    print(f"{changed} card classes changed")


if __name__ == "__main__":
    main()
