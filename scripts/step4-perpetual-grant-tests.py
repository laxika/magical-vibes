"""EffectSlot refactor step 4: card tests that read GameData.perpetualTriggeredAbilityGrants as a
slot-keyed map read GameData.perpetualAbilityEffects(cardId, slot) instead."""
import glob
import re

CONTAINS_KEY = re.compile(r'assertThat\(gd\.perpetualTriggeredAbilityGrants\.get\(([^()]*\([^()]*\)[^()]*|[^()]*)\)\)'
                          r'\s*\.containsKey\(([\w.]+)\);')
GET_SLOT = re.compile(r'gd\.perpetualTriggeredAbilityGrants\.get\(([^()]*\([^()]*\)[^()]*|[^()]*)\)\s*\.get\(([\w.]+)\)')

for path in glob.glob('magical-vibes-card-test/*/src/test/java/**/*Test.java', recursive=True):
    s = open(path).read()
    if 'perpetualTriggeredAbilityGrants.get(' not in s:
        continue
    new = CONTAINS_KEY.sub(r'assertThat(gd.perpetualAbilityEffects(\1, \2)).isNotEmpty();', s)
    new = GET_SLOT.sub(r'gd.perpetualAbilityEffects(\1, \2)', new)
    if new != s:
        open(path, 'w').write(new)
        print(path)
