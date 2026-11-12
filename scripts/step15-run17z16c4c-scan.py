#!/usr/bin/env python3
"""Step 15, run 17z16c4c: the target a "you may" ability chose after its question was answered (read-only).

A triggered ability's targets are chosen as it is put on the stack (CR 603.3d, 115.1); its "you may" is decided as it
resolves (CR 603.5), where a target is only checked again (CR 608.2b). The row named seven sites that chose a target of
the resolving entry once its "you may" was accepted. The scan prints

  sites       the seven sites, each GONE or PRESENT in the engine's main sources;
  writers     every place left that makes a choice for an entry whose resolution is waiting and writes it on that
              entry (`GameData.resolvedMayTargetingEntry = ...`, an `ETBTokenMultiTargetTrigger` built with
              `resumePendingMayResolution`), each explained in `WRITERS` after reading it and its cards' Oracle texts,
              or `UNEXPLAINED`;
  measured    what the probe measured on each site (`MEASURED`, the cards with their Oracle wording and class), checked
              against the card classes that exist;
  candidates  the card classes that write a "you may" around an effect class that declares a target (an upper bound,
              read from the source text), and how many of them have a test class that answers a "you may".

Classes

  (a) "you may [do X to] target ...": a targeted triggered ability; the target is chosen as the ability is put on the
      stack and the "you may" is asked as it resolves
  (b) a choice that is not a target (the text says "choose", or names no target), made as the ability resolves
  (c) the option of a spell or an activated ability that is resolving

The probe (run 17z16c4c's Findings): a line written at each of the seven sites at `7eec666a3a`, over the 5,158 card
test classes `step15-run17z16c4b-test-list.py` lists (45,996 cases). Only `continueAcceptedMayAbility` was reached,
by the 15 cards of `MEASURED` (16 abilities: two chapters of one Saga), and for none of them did it go on to choose a target.

Exits 1 on a PRESENT site, an UNEXPLAINED writer or a measured card class that no longer exists.

Usage: step15-run17z16c4c-scan.py [--summary]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes"
DOMAIN = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes"
CARDS = ROOT / "magical-vibes-card"
CARD_TESTS = ROOT / "magical-vibes-card-test"
MAY_SERVICE = ENGINE / "service/input/MayAbilityHandlerService.java"

# The row's seven sites: (name, file that held it, the text that marks it).
SITES = [
    ("handleResolutionTimeMayChoice: the test of the two attach effect classes", MAY_SERVICE,
     re.compile(r"AttachTarget(?:AuraOr)?EquipmentToTargetCreatureEffect")),
    ("handleResolutionTimeTargetSelection", MAY_SERVICE, re.compile(r"handleResolutionTimeTargetSelection")),
    ("handleResolutionTimeGraveyardTargetSelection", MAY_SERVICE,
     re.compile(r"handleResolutionTimeGraveyardTargetSelection")),
    ("beginExactResolutionTimeGraveyardTargetSelection", MAY_SERVICE,
     re.compile(r"beginExactResolutionTimeGraveyardTargetSelection")),
    ("continueAcceptedMayAbility", MAY_SERVICE, re.compile(r"continueAcceptedMayAbility")),
    ("AttachTargetEquipmentToTargetCreatureHandler: the target walk",
     ENGINE / "service/effect/mayfx/AttachTargetEquipmentToTargetCreatureHandler.java", None),
    ("AttachTargetAuraOrEquipmentToTargetCreatureHandler: the target walk",
     ENGINE / "service/effect/mayfx/AttachTargetAuraOrEquipmentToTargetCreatureHandler.java", None),
]

# What the answer of a resolving entry's "you may" must not do any more (in `MayAbilityHandlerService`).
ANSWER_CHOOSES = re.compile(r"beginPermanentChoice|GraveyardChoice|beginMultiGraveyardChoice|setTargetId\(|"
                            r"setTargetIds\(|setTargetCardIds\(|setDeclaredTargetIds\(|resolvedMayTargetingEntry")

# The writers left: (file under the engine's `service/`, marker) -> (class, why).
WRITERS = {
    ("effect/normalfx/ChooseCardFromGraveyardAndGrantCastPermissionEffectHandler.java", "resolvedMayTargetingEntry"):
        ("b", "Mission Briefing (GRN 44): 'Surveil 2, then choose an instant or sorcery card in your graveyard. You "
              "may cast it this turn.' The card is chosen as the spell resolves; the entry is marked non-targeting"),
    ("effect/normalfx/CastCardFromGraveyardEffectHandler.java", "resolvedMayTargetingEntry"):
        ("b", "'you may cast [a card] from your graveyard' (Diviner of Mist, Spectral Arcanist, Sword of Once and "
              "Future, ...): the card is chosen as the ability resolves, no target is named; the entry is marked "
              "non-targeting"),
    ("effect/mayfx/MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyChoiceHandler.java",
     "resolvedMayTargetingEntry"):
        ("b", "Shadow Kin: 'you may exile a creature card from among the cards milled this way'; the card is chosen "
              "as the ability resolves, on an entry built for the choice"),
    ("input/ChoiceHandlerService.java", "resumePendingMayResolution"):
        ("b/c", "not a 'you may': the targets of the modes chosen as a `ChooseOneEffect` resolves, on a card that "
                "declares target groups. Not this row's subject and not measured by it (run 17z16c4c's Findings, "
                "Not verified)"),
}

# What the probe measured: site -> [(card class, class, the Oracle wording that decides it)].
MEASURED = {
    "handleResolutionTimeMayChoice: the test of the two attach effect classes": [],
    "handleResolutionTimeTargetSelection": [],
    "handleResolutionTimeGraveyardTargetSelection": [],
    "beginExactResolutionTimeGraveyardTargetSelection": [],
    "AttachTargetEquipmentToTargetCreatureHandler: the target walk": [],
    "AttachTargetAuraOrEquipmentToTargetCreatureHandler: the target walk": [],
    "continueAcceptedMayAbility": [
        ("AGoldenOpportunity", "b", "II, III — You may tap an untapped Bird you control and sacrifice an artifact. "
                                    "If you do, conjure a card named Golden Egg onto the battlefield."),
        ("AzizaMageTowerCaptain", "b", "you may tap three untapped creatures you control. If you do, copy that "
                                       "spell. You may choose new targets for the copy."),
        ("CaparoctiSunborn", "b", "you may tap two untapped artifacts and/or creatures you control. If you do, "
                                  "discover 3."),
        ("CivilServant", "b", "you may tap another untapped Citizen you control. If you do, this creature gets +1/+0 "
                              "and gains lifelink until end of turn."),
        ("CommandBridge", "b", "sacrifice it unless you tap an untapped permanent you control."),
        ("GravelgillScoundrel", "b", "you may tap another untapped creature you control. If you do, this creature "
                                     "can't be blocked this turn."),
        ("KittKantoMayhemDiva", "b", "you may tap two untapped creatures you control. When you do, target creature "
                                     "that player controls gets +2/+2 ... (the target is the reflexive ability's, "
                                     "CR 603.12)"),
        ("KoskunFalls", "b", "sacrifice this enchantment unless you tap an untapped creature you control."),
        ("MeandersGuide", "b", "you may tap another untapped Merfolk you control. When you do, return target "
                               "creature card ... (the target is the reflexive ability's, CR 603.12)"),
        ("PublicThoroughfare", "b", "sacrifice it unless you tap an untapped artifact or land you control."),
        ("RaffWeatherlightStalwart", "b", "you may tap two untapped creatures you control. If you do, draw a card."),
        ("RedcapRaiders", "b", "you may tap an untapped non-Human creature you control. If you do, this creature "
                               "gets +1/+1 and gains trample until end of turn."),
        ("RentIsDue", "b", "you may tap two untapped creatures and/or Treasures you control. If you do, draw a "
                           "card. Otherwise, sacrifice this enchantment."),
        ("SaheeliFiligreeMaster", "c", "+1: Scry 1. You may tap an untapped artifact you control. If you do, draw a "
                                       "card. (a loyalty ability)"),
        ("ThousandMoonsSmithy", "b", "you may tap five untapped artifacts and/or creatures you control. If you do, "
                                     "transform Thousand Moons Smithy."),
    ],
}

# The class (a) cards the row named: none reached a site; each chooses its targets as the ability goes on the stack.
NAMED_A = [
    ("KorOutfitter", "When this creature enters, you may attach target Equipment you control to target creature you "
                     "control."),
    ("NoctisHeirApparent", "Whenever a creature you control enters during combat, you may attach target Equipment "
                           "you control to target creature you control."),
    ("HalvarGodOfBattle", "At the beginning of each combat, you may attach target Aura or Equipment attached to a "
                          "creature you control to target creature you control."),
]


def card_class(name):
    return CARDS / name[0].lower() / "src/main/java/com/github/laxika/magicalvibes/cards" / name[0].lower() \
        / (name + ".java")


def targeted_effect_classes():
    return {path.stem for path in (DOMAIN / "model/effect").glob("*.java")
            if "TargetSpec targetSpec()" in path.read_text()}


def main():
    summary = "--summary" in sys.argv
    failed = False
    lines = []

    present = 0
    may_text = MAY_SERVICE.read_text()
    for name, path, marker in SITES:
        there = path.exists() and (marker is None or marker.search(path.read_text()) is not None)
        present += there
        lines.append(f"site {'PRESENT' if there else 'GONE'}: {name}")
    answer = re.search(r"private void handleResolutionTimeMayChoice\(.*?\n    }\n", may_text, re.S)
    tap = re.search(r"public void continueAfterTapCost\(.*?\n    }\n", may_text, re.S)
    chooses = [m.group(0) for body in (answer, tap) if body for m in ANSWER_CHOOSES.finditer(body.group(0))]
    if answer is None or tap is None or chooses:
        present += 1
        lines.append(f"site PRESENT: the answer of a resolving entry's \"you may\" still chooses ({chooses})")
    failed |= present > 0

    unexplained = 0
    writers = 0
    service = ENGINE / "service"
    for path in sorted(service.glob("**/*.java")):
        text = path.read_text()
        rel = str(path.relative_to(service))
        markers = []
        if re.search(r"resolvedMayTargetingEntry = (?!null)", text):
            markers.append("resolvedMayTargetingEntry")
        for call in re.finditer(r"new PermanentChoiceContext\.ETBTokenMultiTargetTrigger\((.*?)\);", text, re.S):
            arguments = call.group(1).rstrip(") \n")
            if re.search(r",\s*true\s*$", arguments) or re.search(r",\s*true\s*,\s*null", arguments):
                markers.append("resumePendingMayResolution")
        for marker in sorted(set(markers)):
            writers += 1
            explained = WRITERS.get((rel, marker))
            if explained is None:
                unexplained += 1
                lines.append(f"writer UNEXPLAINED: {rel} ({marker})")
            else:
                lines.append(f"writer ({explained[0]}) {rel} ({marker}): {explained[1]}")
    failed |= unexplained > 0

    by_class = {}
    missing = 0
    for site, cards in MEASURED.items():
        lines.append(f"measured {site}: {len(cards)} cards")
        for name, verdict, wording in cards:
            by_class[verdict] = by_class.get(verdict, 0) + 1
            exists = card_class(name).exists()
            missing += not exists
            lines.append(f"    ({verdict}) {name}{'' if exists else ' MISSING'}: {wording}")
    for name, wording in NAMED_A:
        exists = card_class(name).exists()
        missing += not exists
        lines.append(f"named (a), reached no site, targets chosen on the stack: {name}"
                     f"{'' if exists else ' MISSING'}: {wording}")
    failed |= missing > 0

    targeted = targeted_effect_classes()
    wraps = re.compile(r"new (?:MayEffect\(|MayPayManaEffect\(\s*\"[^\"]*\",)\s*(?:SequenceEffect\.of\(\s*)?new (\w+)\(")
    candidates = 0
    answered = 0
    for path in sorted(CARDS.glob("*/src/main/java/**/*.java")):
        if not any(match.group(1) in targeted for match in wraps.finditer(path.read_text())):
            continue
        candidates += 1
        letter = path.stem[0].lower()
        test = (CARD_TESTS / letter / "src/test/java/com/github/laxika/magicalvibes/cards" / letter
                / (path.stem + "Test.java"))
        if test.exists() and "handleMayAbilityChosen" in test.read_text():
            answered += 1
        elif not summary:
            lines.append(f"candidate without an answering test: {path.stem}")

    if summary:
        lines = []
    lines.append(f"sites: {len(SITES)}, {present} PRESENT")
    lines.append(f"writers left on a waiting entry: {writers}, {unexplained} UNEXPLAINED")
    lines.append("measured on the seven sites: (a) 0 cards, "
                 + ", ".join(f"({verdict}) {count} cards" for verdict, count in sorted(by_class.items()))
                 + f"; {missing} MISSING")
    lines.append(f"candidates (a \"you may\" around an effect class that declares a target): {candidates} card "
                 f"classes, {answered} with a test class that answers a \"you may\", {candidates - answered} without")
    print("\n".join(lines))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
