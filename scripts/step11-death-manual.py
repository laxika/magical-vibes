#!/usr/bin/env python3
"""The creature-death cards step11-death-triggers.py leaves alone (its MANUAL list), converted by exact replacements:
"one or more" batches become TriggerOptions.ONE_OR_MORE, Soul Shredder's ability functions from two zones, planes'
abilities function from the command zone, Desperate Measures watches its target with a delayed "when this dies"
trigger, and the "this or another" cards whose shape the main script doesn't know merge into one ability."""
import os
import re
import sys

EDITS = {
    "GenestealerPatriarch": [(
        """        addTrigger(Triggers.dies(ObjectRelation.THIS), childrenOfTheCult);
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), childrenOfTheCult);""",
        """        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), childrenOfTheCult);""")],
    "Lifeline": [(
        """        // Other creatures dying, and Lifeline itself when it is an animated creature that dies.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), returnDyingCreature);
        addTrigger(Triggers.dies(ObjectRelation.THIS), returnDyingCreature);""",
        """        // Lifeline itself counts when it is an animated creature that dies.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), returnDyingCreature);""")],
    "VengefulTownsfolk": [(
        """        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new PutCountersOnSourceEffect(1, 1, 1));""")],
    "SpectacleOfDestruction": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.WRECK)));""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY)), TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.WRECK));""")],
    "SeerOfStolenSight": [(
        """        addEffect(EffectSlot.ON_ALLY_ARTIFACT_OR_CREATURE_DIES,
                new OneOrMoreArtifactOrCreatureDeathTriggerEffect(new SurveilEffect(1)));""",
        """        addTrigger(new TriggerCondition(Triggers.dies(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new SurveilEffect(1));""")],
    "GreatFierceBee": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(new ScryEffect(1)));""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANOTHER)), TriggerOptions.ONE_OR_MORE,
                new ScryEffect(1));""")],
    "Chainsaw": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(
                        new PutCountersOnSourceCardEffect(CounterType.REV), true));""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY)), TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSourceCardEffect(CounterType.REV));""")],
    "BloodSpatterAnalysis": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new OneOrMoreCreatureDeathTriggerEffect(SequenceEffect.of(""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY)), TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of("""), (
        """                                .targetGraveyard(true)
                                .build())))));""",
        """                                .targetGraveyard(true)
                                .build()))));""")],
    "DesperateMeasures": [(
        """                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_ANY_CREATURE_DIES,
                        new DrawCardForTargetPlayerEffect(2), true));""",
        """                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new DrawCardForTargetPlayerEffect(2)),
                        true));""")],
    "Agyrem": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new com.github""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github"""), (
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new com.github""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, new com.github""")],
    "GrandOssuary": [(
        """        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                DistributeCountersAmongCreaturesOnDeathEffect""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT,
                DistributeCountersAmongCreaturesOnDeathEffect""")],
    "KamiOfMourning": [(
        """                new PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(
                        EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES,
                        new TriggeringCardConditionalEffect(
                                new CardManaValueGreaterThanSourceManaValuePredicate(),
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true))));""",
        """                new PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(new TriggeredAbility(
                        new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD),
                        new TriggeringCardConditionalEffect(
                                new CardManaValueGreaterThanSourceManaValuePredicate(),
                                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))));""")],
    "SoulShredder": [(
        """        OneOrMoreCreatureDeathTriggerEffect deathTrigger = new OneOrMoreCreatureDeathTriggerEffect(
                new PerpetuallyBoostSourceEffect(1, 1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, deathTrigger);
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES, deathTrigger);
        addEffect(EffectSlot.GRAVEYARD_ON_OPPONENT_CREATURE_DIES, deathTrigger);""",
        """        // Whenever one or more other creatures die, Soul Shredder perpetually gets +1/+1. This ability also
        // triggers if Soul Shredder is in your graveyard.
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANOTHER),
                        AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD)),
                TriggerOptions.ONE_OR_MORE, new PerpetuallyBoostSourceEffect(1, 1));""")],
    "RinoaAngelWing": [(
        """        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(new OneOrMoreCreatureDeathTriggerEffect(
                        returnAttacker, new PermanentIsAttackingPredicate())));""",
        """        addTrigger(new TriggerCondition(Triggers.creatureDies(new PermanentIsAttackingPredicate(),
                        ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new OncePerTurnTriggerEffect(returnAttacker));""")],
    "VeinRipper": [(
        """        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ANY_CREATURE_DIES, SequenceEffect.of(""",
        """        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.creatureDies(ObjectRelation.ANY), SequenceEffect.of(""")],
    "ArnynDeathbloomBotanist": [(
        """        )).addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, CONDITIONAL_DRAIN)
                .addTrigger(Triggers.dies(ObjectRelation.THIS), CONDITIONAL_DRAIN);""",
        """        )).addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), CONDITIONAL_DRAIN);""")],
    "HostOfTheHereafter": [(
        """        deathTrigger.addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MoveDyingSourceCountersToTargetCreatureEffect(true));
        deathTrigger.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(""",
        """        // Whenever this creature or another creature you control dies, if it had counters on it, ...
        deathTrigger.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(""")],
    "ReluctantRoleModel": [(
        """        deathTrigger.addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MoveDyingSourceCountersToTargetCreatureEffect());
        deathTrigger.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(""",
        """        // Whenever this creature or another creature you control dies, if it had counters on it, ...
        deathTrigger.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(""")],
}

IMPORTS = ["com.github.laxika.magicalvibes.model.trigger.Triggers",
           "com.github.laxika.magicalvibes.model.trigger.ObjectRelation",
           "com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
           "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
           "com.github.laxika.magicalvibes.model.AbilityZones",
           "com.github.laxika.magicalvibes.model.Zone",
           "com.github.laxika.magicalvibes.model.TriggeredAbility",
           "com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate",
           "com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate",
           "java.util.List", "java.util.Set"]


def main():
    paths = {}
    for root, _, files in os.walk("magical-vibes-card"):
        for name in files:
            if name[:-5] in EDITS:
                paths[name[:-5]] = os.path.join(root, name)
    for card, edits in EDITS.items():
        path = paths[card]
        before = open(path).read()
        src = open(path).read()
        for old, new in edits:
            if old not in src:
                if new in src:
                    continue
                sys.exit("%s: text not found:\n%s" % (card, old))
            src = src.replace(old, new, 1)
        body = re.sub(r"^import .*$", "", src, flags=re.M)
        for fqcn in IMPORTS:
            simple = fqcn.rsplit(".", 1)[1]
            if re.search(r"\b%s\b" % simple, body) and not re.search(r"^import [\w.]+\.%s;$" % simple, src, re.M):
                src = add_import(src, fqcn)
        for m in list(re.finditer(r"^import ([\w.]+)\.(\w+);\n", src, re.M)):
            if not re.search(r"\b%s\b" % m.group(2), re.sub(r"^import .*$", "", src, flags=re.M)):
                src = src.replace(m.group(0), "", 1)
        if src != before:
            open(path, "w").write(src)
            print("converted:", path)


def add_import(src, fqcn):
    line = "import %s;" % fqcn
    is_java = fqcn.startswith("java")
    imports = [m for m in re.finditer(r"^import [\w.]+;$", src, re.M)
               if m.group(0).startswith("import java") == is_java]
    if not imports:
        last = list(re.finditer(r"^import [\w.]+;$", src, re.M))[-1]
        return src[:last.end()] + "\n\n" + line + src[last.end():]
    for m in imports:
        if m.group(0) > line:
            return src[:m.start()] + line + "\n" + src[m.start():]
    return src[:imports[-1].end()] + "\n" + line + src[imports[-1].end():]


if __name__ == "__main__":
    main()
