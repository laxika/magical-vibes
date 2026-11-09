#!/usr/bin/env python3
"""Step 15, run 17z12b: the cards whose enter or turned-face-up abilities a class-keyed collector handler bound.

The handlers baked the entering permanent into the entry as `targetId` or as the ability's source for these effect
classes; the cards now say what the effect reads: "that creature" / "for each of them" for the token copies, "its
controller" for the draw and life loss, "it explores" and "it deals" for the entering creature (the triggering
permanent, as it is or as it last existed on the battlefield). Each edit is checked to apply exactly once.
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def card(name):
    found = [p for p in CARDS.glob(f"*/src/main/java/com/github/laxika/magicalvibes/cards/*/{name}.java")]
    assert len(found) == 1, (name, found)
    return found[0]


def add_import(text, fqcn):
    line = f"import {fqcn};\n"
    if line in text:
        return text
    imports = [i for i, l in enumerate(text.split("\n")) if l.startswith("import ")]
    lines = text.split("\n")
    lines.insert(imports[-1] + 1, line.rstrip("\n"))
    return "\n".join(lines)


def remove_import_if_unused(text, simple, fqcn):
    body = "\n".join(l for l in text.split("\n") if not l.startswith("import "))
    if simple not in body:
        text = text.replace(f"import {fqcn};\n", "")
    return text


EFFECT = "com.github.laxika.magicalvibes.model.effect."
AMOUNT = "com.github.laxika.magicalvibes.model.amount."
EDITS = {
    "Necroduality": [("new CreateTokenCopyOfTargetPermanentEffect());",
                      "new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent());")],
    "TwilightDiviner": [("new CreateTokenCopyOfTargetPermanentEffect());",
                         "new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent());")],
    "MiirymSentinelWyrm": [("CreateTokenCopyOfTargetPermanentEffect.nonLegendary(List.of(), Set.of(), null, null, Map.of()));",
                            "CreateTokenCopyOfTargetPermanentEffect.nonLegendary(List.of(), Set.of(), null, null, Map.of())\n"
                            "                        .ofTriggeringPermanent());")],
    "FirionWildRoseWarrior": [("true, List.of(StaticAbility.of(List.of(ReduceEquipCostEffect.ofThisEquipmentOnly(2))))));",
                               "true, List.of(StaticAbility.of(List.of(ReduceEquipCostEffect.ofThisEquipmentOnly(2)))))\n"
                               "                        .ofTriggeringPermanent());")],
    "IsleOfVesuva": [("                        false, true, null, Set.of()));",
                      "                        false, true, null, Set.of()).ofTriggeringPermanent());")],
    "KambalProfiteeringMayor": [("CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy());",
                                 "CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy().ofEachTriggeringPermanent());")],
    "FaerieArtisans": [("new Fixed(1), false, Set.of(), false);",
                        "new Fixed(1), false, Set.of(), false).ofTriggeringPermanent();")],
    "KavuLair": [("new DrawCardForTargetPlayerEffect(1));", "new DrawCardForTriggeringPermanentControllerEffect());")],
    "PoisonbellyOgre": [("new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));",
                         "new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));")],
    "PathOfDiscovery": [("addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new ExploreEffect());",
                         "addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),\n"
                         "                new ExploreEffect(PermanentReference.TRIGGERING));")],
    "WarstormSurge": [("new DealDamageToAnyTargetEffect(new SourcePower()));",
                       "DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower()));")],
    "BelakorTheDarkMaster": [("new DealDamageToAnyTargetEffect(new SourcePower()));",
                              "DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower()));")],
    "TerrorOfThePeaks": [("new DealDamageToAnyTargetEffect(new ChosenPermanentPower()));",
                          "DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower()));")],
    "DragonTempest": [("                new DealDamageToAnyTargetEffect(new PermanentCount(\n"
                       "                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER)));",
                       "                DealDamageToAnyTargetEffect.fromEnteringPermanent(new PermanentCount(\n"
                       "                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER)));"),
                      ("this enchantment deals X damage to any target", "it deals X damage to any target")],
    "HawkeyeTrickShot": [("        return new DealDamageToAnyTargetEffect(new PermanentCount(",
                          "        return DealDamageToAnyTargetEffect.fromEnteringPermanent(new PermanentCount(")],
    "ScourgeOfValkas": [("        return new DealDamageToAnyTargetEffect(\n",
                         "        return DealDamageToAnyTargetEffect.fromEnteringPermanent(\n")],
    "ChampionOfThePath": [("new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));",
                           "new DealDamageToPlayersEffect(new TriggeringPermanentPower(), DamageRecipient.EACH_OPPONENT)\n"
                           "                        .dealtByTriggeringPermanent());")],
    "PyrotechnicPerformer": [("new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));",
                              "new DealDamageToPlayersEffect(new TriggeringPermanentPower(), DamageRecipient.EACH_OPPONENT)\n"
                              "                        .dealtByTriggeringPermanent());")],
}
IMPORTS = {
    "KavuLair": [(EFFECT + "DrawCardForTriggeringPermanentControllerEffect", None),
                 (EFFECT + "DrawCardForTargetPlayerEffect", "remove")],
    "PathOfDiscovery": [("com.github.laxika.magicalvibes.model.effect.PermanentReference", None)],
    "TerrorOfThePeaks": [(AMOUNT + "SourcePower", None), (AMOUNT + "ChosenPermanentPower", "remove")],
    "ChampionOfThePath": [(AMOUNT + "TriggeringPermanentPower", None), (AMOUNT + "SourcePower", "remove")],
    "PyrotechnicPerformer": [(AMOUNT + "TriggeringPermanentPower", None), (AMOUNT + "SourcePower", "remove")],
}

for name, edits in EDITS.items():
    path = card(name)
    text = path.read_text()
    for old, new in edits:
        assert text.count(old) == 1, (name, old)
        text = text.replace(old, new)
    for fqcn, mode in IMPORTS.get(name, []):
        if mode == "remove":
            text = remove_import_if_unused(text, fqcn.rsplit(".", 1)[1], fqcn)
        else:
            text = add_import(text, fqcn)
    path.write_text(text)
    print("converted", name)
