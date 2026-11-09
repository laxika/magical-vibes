#!/usr/bin/env python3
"""Step 15, run 17z11f: "whenever this creature becomes the target of a spell or ability for the first time each turn"
is the trigger's FIRST_TIME_EACH_TURN limit (Glyph Keeper, Shimmering Glasskite, Jetting Glasskite, Kira, Great Glass-
Spinner), no longer the becomes-the-target collector's reading of CounterSpellEffect. Idempotent; optional
repository-root argument."""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
CARDS = root / 'magical-vibes-card'

OLD_COMMENT = '''        // Whenever this creature becomes the target of a spell or ability for the first time each turn,
        // counter that spell or ability. The "first time each turn" gating and targeting the triggering
        // object are handled in TriggerCollectionService for any counterspelling effect in this slot.
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new CounterSpellEffect());'''
NEW_COMMENT = '''        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new CounterSpellEffect());'''
KIRA_OLD = '''        // Creatures you control have "Whenever this creature becomes the target of a spell or ability
        // for the first time each turn, counter that spell or ability." ALL_OWN_CREATURES includes Kira
        // itself. The "first time each turn" gating and targeting the triggering object are handled in
        // TriggerCollectionService for any counterspelling effect in this slot (as for Glyph Keeper).
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                new CounterSpellEffect()),
                GrantScope.ALL_OWN_CREATURES));'''
KIRA_NEW = '''        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                        new CounterSpellEffect()),
                GrantScope.ALL_OWN_CREATURES));'''
IMPORTS = ['import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;',
           'import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;']


def add_imports(text):
    for imp in IMPORTS:
        if imp not in text:
            anchor = 'import com.github.laxika.magicalvibes.model.trigger.Triggers;'
            assert anchor in text
            text = text.replace(anchor, imp + '\n' + anchor)
    return text


def convert(path, old, new):
    text = path.read_text()
    if new in text:
        print('unchanged', path.name)
        return
    assert text.count(old) == 1, path
    path.write_text(add_imports(text.replace(old, new)))
    print('converted', path.name)


for letter, name in (('g', 'GlyphKeeper'), ('s', 'ShimmeringGlasskite'), ('j', 'JettingGlasskite')):
    convert(CARDS / letter / f'src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java',
            OLD_COMMENT, NEW_COMMENT)
convert(CARDS / 'k/src/main/java/com/github/laxika/magicalvibes/cards/k/KiraGreatGlassSpinner.java', KIRA_OLD, KIRA_NEW)
