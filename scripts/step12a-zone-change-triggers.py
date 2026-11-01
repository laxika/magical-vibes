#!/usr/bin/env python3
"""Moves card classes and card tests off the zone-change EffectSlots of step 12a (effect-slot-refactor.md).

Every registration in a slot of FAMILIES becomes a triggered ability with a real trigger: the slot's trigger with the
relation its Oracle wording gives ("another" excludes the source, "this or another" always includes it), the zone the
old slot's prefix named, and the multiplicity ("one or more" is TriggerOptions.ONE_OR_MORE). These triggers are the
keys of the slot's ZoneChangeWatch in the engine, so the two must stay the same. The rest follows the Oracle text:

* RELATION gives a card's relation where it isn't the slot's default;
* OPTIONS gives a card's options where it isn't TriggerOptions.DEFAULT;
* DROP_SELF names the "when this …" registration a card added only so its own move counted, now that the ability
  includes its source: the statement is removed;
* grants (GrantTriggeredAbilityEffect and the like) take the ability.

Usage: python3 scripts/step12a-zone-change-triggers.py FAMILY DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")
step11.IMPORTS["new EffectRegistration("] = "com.github.laxika.magicalvibes.model.EffectRegistration"
step11.IMPORTS["PlayerRelation.OPPONENT)"] = "com.github.laxika.magicalvibes.model.trigger.PlayerRelation"
step11.IMPORTS["PlayerRelation.YOU)"] = "com.github.laxika.magicalvibes.model.trigger.PlayerRelation"
step11.IMPORTS["Set.of("] = "java.util.Set"
step11.IMPORTS["(Zone."] = "com.github.laxika.magicalvibes.model.Zone"

# slot -> (trigger template with {rel}, default relation, zone the ability functions from or None for the battlefield)
FAMILIES = {
    "sacrifice": {
        "ON_ALLY_PERMANENT_SACRIFICED": ("Triggers.sacrificed(null, ObjectRelation.{rel})", "YOU_CONTROL", None),
        "ON_OPPONENT_PERMANENT_SACRIFICED": ("Triggers.sacrificed(null, ObjectRelation.{rel})", "OPPONENT_CONTROLS",
                                             None),
        "ON_ANY_PERMANENT_SACRIFICED": ("Triggers.sacrificed(null, ObjectRelation.{rel})", "ANY", None),
        "ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED": (
            "Triggers.sacrificedOrDestroyed(Triggers.NONCREATURE_ARTIFACT, ObjectRelation.{rel})", "ANY", None),
        "ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED": ("Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.{rel})",
                                                      "OPPONENT_CONTROLS", None),
        "ON_ANY_CREATURE_SACRIFICED": ("Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.{rel})", "ANY", None),
        "EXILE_ON_CONTROLLER_TOKEN_SACRIFICED": ("Triggers.sacrificed(Triggers.TOKEN, ObjectRelation.{rel})",
                                                 "YOU_CONTROL", "EXILE"),
        "GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED": ("Triggers.sacrificed(null, ObjectRelation.{rel})",
                                                         "YOU_CONTROL", "GRAVEYARD"),
    },
    # Each slot keeps the relation its collector gave it, whether the source's own move counted included; the
    # collectors' wrappers still narrow it (step 14 turns them into filters).
    "graveyard": {
        "ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.creatureDies(ObjectRelation.{rel})", "YOU_OWN", None),
        "GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.creatureDies(ObjectRelation.{rel})", "YOU_OWN", "GRAVEYARD"),
        "GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.creatureDies(ObjectRelation.{rel})", "OPPONENT_OWNS", "GRAVEYARD"),
        "ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.{rel})", "ANY", None),
        "ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.{rel})", "ANY", None),
        "ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.{rel})", "YOU_CONTROL", None),
        "ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.{rel})", "OPPONENT_CONTROLS", None),
        "ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "OPPONENT_CONTROLS", None),
        # "Owned by another player" is "put into an opponent's graveyard" (CR 400.3).
        "ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "ANY", None),
        "ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.NONTOKEN, ObjectRelation.{rel})", "ANY", None),
        "ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.NONTOKEN, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL", None),
        "ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(null, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT": (
            "Triggers.dies(Triggers.LAND, ObjectRelation.{rel}).causedBy(PlayerRelation.OPPONENT)", "YOU_OWN", None),
        "ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT": (
            "Triggers.destroyed(Triggers.NONCREATURE, ObjectRelation.{rel}).causedBy(PlayerRelation.OPPONENT)",
            "YOU_CONTROL", None),
        "ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.LAND, ObjectRelation.{rel})", "ANY", None),
        "ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD": ("Triggers.dies(ObjectRelation.{rel})", "ENCHANTED", None),
        "ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD": (
            "Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.{rel})", "YOU_CONTROL", None),
    },
    "leaves": {
        "ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(null, ObjectRelation.{rel})", "ENCHANTED", None),
        "ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(null, ObjectRelation.{rel})", "ANOTHER", None),
        "ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.{rel})", "ANOTHER", None),
        "ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(Triggers.ARTIFACT, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL", None),
        "ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD": (
            "Triggers.putIntoGraveyardOrExileFromBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.{rel})",
            "ANOTHER_YOU_CONTROL", None),
        "ON_ALLY_CREATURE_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL", None),
        "ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD": (
            "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.{rel})",
            "ANOTHER_YOU_CONTROL", None),
        "ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD": (
            "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.{rel})", "ANY", None),
        "ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD": (
            "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.ARTIFACT, ObjectRelation.{rel})", "ANOTHER", None),
        "ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING": (
            "Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL",
            None, "ONE_OR_MORE"),
        "ON_ALLY_CREATURE_LEAVES_BATTLEFIELD_WITHOUT_DYING": (
            "Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL",
            None),
        "ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING": (
            "Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.{rel})",
            "THIS_OR_ANOTHER_YOU_CONTROL", None),
        "ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN": (
            "Triggers.leavesBattlefield(null, ObjectRelation.{rel}).onlyDuringYourTurn()", "ANOTHER_YOU_CONTROL", None),
        "ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD": (
            "Triggers.leavesBattlefield(null, ObjectRelation.{rel})", "ANOTHER_YOU_CONTROL", None),
        "ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD": (
            "Triggers.leavesBattlefield(null, ObjectRelation.{rel})", "YOU_CONTROL", None, "ONE_OR_MORE"),
        "ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT": (
            "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.{rel}).causedBy(PlayerRelation.YOU)",
            "ANOTHER", None),
        "ON_ANY_PERMANENT_RETURNED_TO_HAND": (
            "Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.{rel})", "ANY", None),
        "ON_CONTROLLER_CREATURE_RETURNED_TO_HAND": (
            "Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.CREATURE, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND": (
            "Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_CONTROLLER_ANOTHER_NONLAND_PERMANENT_RETURNED_TO_HAND": (
            "Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.NONLAND, ObjectRelation.{rel})",
            "ANOTHER_YOU_CONTROL", None),
    },
    "outside": {
        "ON_ANY_CARD_EXILED": ("Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.{rel})", "ANOTHER", None,
                               "ONE_OR_MORE"),
        "ON_CONTROLLER_CARDS_EXILED_DURING_TURN": (
            "Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.{rel}).onlyDuringYourTurn()", "ANY", None,
            "ONE_OR_MORE"),
        "ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE": ("Triggers.putIntoCommandZone(null, ObjectRelation.{rel})",
                                                    "YOU_OWN", None),
        "ON_CONTROLLER_CARDS_EXILED_FROM_HAND": (
            "Triggers.exiledFrom(Set.of(Zone.HAND), null, ObjectRelation.{rel})", "YOU_OWN", None, "ONE_OR_MORE"),
        "ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY": (
            "Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_ALLY_LAND_CARD_MILLED": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.PERMANENT_CARD, ObjectRelation.{rel})", "YOU_OWN",
            None),
        "ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})",
            "YOU_OWN", None, "ONE_OR_MORE"),
        "ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.{rel})", "YOU_OWN", None,
            "ONE_OR_MORE"),
        "ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "ANY",
            None),
        "ON_ANY_CARDS_PUT_INTO_LIBRARY": ("Triggers.putIntoLibrary(Triggers.NONTOKEN, ObjectRelation.{rel})", "ANOTHER", None,
                                          "ONE_OR_MORE"),
        "ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.{rel})", "ANOTHER_YOU_OWN", None),
        "ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD": (
            "Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.{rel})",
            "YOU_OWN", None),
        "ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.{rel})", "ANOTHER", None),
        "ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "ANOTHER_YOU_OWN", None),
        "ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD": (
            "Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})",
            "ANY", None),
        "ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.{rel})", "ANOTHER_YOU_OWN", None),
        "ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.BLACK_CARD, ObjectRelation.{rel})", "OPPONENT_OWNS", None),
        "GRAVEYARD_ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE": ("Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.{rel})", "ANY", "GRAVEYARD"),
        "ON_OPPONENT_MILLS": ("Triggers.mills(null, ObjectRelation.{rel})", "OPPONENT_OWNS", None, "ONE_OR_MORE"),
        "ON_NONLAND_CARDS_MILLED": ("Triggers.mills(Triggers.NONLAND, ObjectRelation.{rel})", "ANY", None,
                                    "ONE_OR_MORE"),
        "ON_ANY_NONLAND_CARDS_MILLED": ("Triggers.mills(Triggers.NONLAND, ObjectRelation.{rel})", "ANY", None,
                                        "ONE_OR_MORE"),
        "GRAVEYARD_ON_OPPONENT_NONLAND_CARD_MILLED": ("Triggers.milled(Triggers.NONLAND, ObjectRelation.{rel})",
                                                      "OPPONENT_OWNS", "GRAVEYARD"),
        "ON_OPPONENT_CREATURE_CARD_MILLED": (
            "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})",
            "OPPONENT_OWNS", None),
        "GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD": (
            "Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "OPPONENT_OWNS", "GRAVEYARD"),
        "GRAVEYARD_ON_SELF_RETURNED_TO_HAND": ("Triggers.putIntoHandFrom(Zone.GRAVEYARD, null, ObjectRelation.{rel})",
                                               "THIS", "GRAVEYARD"),
        "ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD": ("Triggers.leavesGraveyard(null, ObjectRelation.{rel})", "YOU_OWN",
                                                None, "ONE_OR_MORE"),
        "ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND": (
            "Triggers.putIntoHandFrom(Zone.GRAVEYARD, null, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD": (
            "Triggers.leavesGraveyard(null, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD": (
            "Triggers.exiledFrom(Set.of(Zone.GRAVEYARD), null, ObjectRelation.{rel})", "YOU_OWN", None, "ONE_OR_MORE"),
        "ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD": (
            "Triggers.exiledFrom(Set.of(Zone.LIBRARY, Zone.GRAVEYARD), null, ObjectRelation.{rel})", "YOU_OWN", None,
            "ONE_OR_MORE"),
        "ON_OPPONENT_OWNED_CARD_EXILED": ("Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.{rel})",
                                          "OPPONENT_OWNS", None, "ONE_OR_MORE"),
        "ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN": (
            "Triggers.exiledFrom(Set.of(Zone.GRAVEYARD, Zone.BATTLEFIELD), Triggers.NONTOKEN, ObjectRelation.{rel})"
            ".onlyDuringYourTurn()", "ANY", None, "ONE_OR_MORE"),
        "ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD": (
            "Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "YOU_OWN", None,
            "ONE_OR_MORE"),
        "ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD": (
            "Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.{rel})", "YOU_OWN", None),
        "ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD": (
            "Triggers.leavesGraveyard(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.{rel})", "YOU_OWN", None,
            "ONE_OR_MORE"),
        "ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD": (
            "Triggers.leavesGraveyard(Triggers.ARTIFACT, ObjectRelation.{rel})", "YOU_OWN", None, "ONE_OR_MORE"),
    },
}

# A registration's TriggerMode, where it says something the trigger or its options now say.
TRIGGER_MODES = {
    # Once for a batch: "whenever one or more …" (CR 603.2c).
    "ONCE_PER_BATCH": ("options", "ONE_OR_MORE"),
    # Voracious Brood counts the cards a library puts into the graveyard with another ability.
    "LIBRARY_ARRIVALS_BATCHED": ("trigger",
                                 "Triggers.putIntoGraveyardFromAnywhereBut(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, "
                                 "ObjectRelation.ANOTHER_YOU_OWN)"),
}

ANOTHER_YOU_CONTROL = "ANOTHER_YOU_CONTROL"
RELATION = {
    # "Whenever you sacrifice another …"
    **{(card, "ON_ALLY_PERMANENT_SACRIFICED"): ANOTHER_YOU_CONTROL for card in """
        ZhaoRuthlessAdmiral VitoFanaticOfAclazotz SandbenderScavengers SzarelGenesisShepherd RakdosTheMuscle
        PiratePeddlers ProwlingGeistcatcher LightlessEvangel SorcerersBroom GiantFly GixianInfiltrator Fleshtaker
        FurnaceCelebration BagOfDevouring Bloodbriar BodyDropper AshadTheLoneCyberman ForgeBoss
        """.split()},
    # "This or another [restricted] permanent": the restriction is still the effect's conditional wrapper, which
    # would reject the source once it lost the characteristic, so the card keeps its "when you sacrifice this"
    # ability and this one covers the others (step 14 merges them, as step 11's split death cards).
    ("SlayersBounty", "ON_ALLY_PERMANENT_SACRIFICED"): ANOTHER_YOU_CONTROL,
    ("EsotericDuplicator", "ON_ALLY_PERMANENT_SACRIFICED"): ANOTHER_YOU_CONTROL,
    # "Whenever you sacrifice [this] or another creature", with no ability of its own for its sacrifice.
    ("KingpinWilsonFisk", "ON_ALLY_PERMANENT_SACRIFICED"): "THIS_OR_ANOTHER_YOU_CONTROL",
    ("EffluenceDevourer", "ON_ALLY_PERMANENT_SACRIFICED"): "THIS_OR_ANOTHER_YOU_CONTROL",
    # "Whenever a player sacrifices another …"
    ("MazirekKraulDeathPriest", "ON_ANY_PERMANENT_SACRIFICED"): "ANOTHER",
    ("ZodiarkUmbralGod", "ON_ANY_CREATURE_SACRIFICED"): "ANOTHER",
}

OPTIONS = {
    # "Whenever you sacrifice one or more Foods" (CR 603.2c).
    ("CamelliaTheSeedmiser", "ON_ALLY_PERMANENT_SACRIFICED"): "ONE_OR_MORE",
}

# card -> the "when this …" trigger whose registration only duplicated the slot ability for the source's own move.
DROP_SELF = {
    "KorvoldFaeCursedKing": "Triggers.sacrificed(ObjectRelation.THIS)",
}

GRANT_HEADS = ("new GrantTriggeredAbilityEffect", "new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect")


def drop_self(src, trigger):
    """Drops the top-level "addTrigger(<trigger>, ...);" statement."""
    m = re.search(r"^([ \t]*)addTrigger\(%s," % re.escape(trigger), src, re.M)
    if m is None:
        raise ValueError("no %s registration to drop" % trigger)
    close = step11.find_close(src, src.index("(", m.start()))
    end = src.index(";", close) + 1
    if src[end:end + 1] == "\n":
        end += 1
    return src[:m.start()] + src[end:]


def trigger_head(card, slot, family, mode_options=None):
    template, default_relation, zone = family[slot][:3]
    trigger = template.format(rel=RELATION.get((card, slot), default_relation))
    options = mode_options or OPTIONS.get((card, slot), family[slot][3] if len(family[slot]) > 3 else None)
    if zone is None and options is None:
        return trigger, None
    condition = "new TriggerCondition(%s%s)" % (trigger, ", AbilityZones.%s" % zone if zone else "")
    return trigger, "%s, TriggerOptions.%s" % (condition, options or "DEFAULT")


def convert_calls(src, card, family):
    slot_re = "|".join(family)
    call = re.compile(r"(addEffect|LegacySlots\.triggered|%s)\((\s*)EffectSlot\.(%s)\s*,(\s*)"
                      % ("|".join(re.escape(h) for h in GRANT_HEADS), slot_re))
    out, pos = [], 0
    for m in call.finditer(src):
        if m.start() < pos:
            continue
        close = step11.find_close(src, src.index("(", m.start()))
        slot = m.group(3)
        lead, gap = m.group(2), m.group(4)
        sep = "," + gap if "\n" in gap else ", "
        rest = src[m.end():close]
        mode = re.search(r",\s*TriggerMode\.(\w+)\s*$", rest)
        override = None
        mode_options = None
        if mode:
            if mode.group(1) not in TRIGGER_MODES:
                raise ValueError("%s: a %s registration has TriggerMode.%s" % (card, slot, mode.group(1)))
            kind, value = TRIGGER_MODES[mode.group(1)]
            rest = rest[:mode.start()]
            if kind == "options":
                mode_options = value
            else:
                override = value
        elif re.search(r"\bTriggerMode\.", rest):
            raise ValueError("%s: a %s registration has a TriggerMode" % (card, slot))
        trigger, condition = trigger_head(card, slot, family, mode_options)
        if override is not None:
            trigger, condition = override, None
        head = m.group(1)
        if head == "LegacySlots.triggered":
            if OPTIONS.get((card, slot)):
                raise ValueError("%s: a LegacySlots.triggered %s needs options" % (card, slot))
            effects = rest.strip()
            list_of = re.match(r"List\.(<\w+>)?of\(", effects)
            if list_of and step11.find_close(effects, list_of.end() - 1) == len(effects) - 1:
                effects = effects[list_of.end():-1]
            zone = family[slot][2]
            if len(family[slot]) > 3:
                raise ValueError("%s: a LegacySlots.triggered %s needs options" % (card, slot))
            head_expr = trigger if zone is None else "new TriggerCondition(%s, AbilityZones.%s)" % (trigger, zone)
            body = "new TriggeredAbility(%s%s%s)" % (head_expr, sep, effects)
        elif head == "addEffect":
            body = "addTrigger(%s%s%s)" % (condition or trigger, sep, rest)
        else:
            args = step11.split_args(rest)
            first = args[0]
            first_end = rest.index(first) + len(first)
            if condition is not None and family[slot][2] is not None:
                raise ValueError("%s: a grant of %s needs a zone" % (card, slot))
            if condition is not None:
                options = condition.rsplit(", ", 1)[1]
                ability = "new TriggeredAbility(%s, %s,%s        List.of(new EffectRegistration(%s)))" % (
                    trigger, options, "\n" + " " * (len(lead) - len(lead.lstrip("\n")) + 8), first)
            else:
                ability = "new TriggeredAbility(%s%s%s)" % (trigger, sep, first)
            body = "%s(%s%s%s)" % (head, lead, ability, rest[first_end:])
        out.append(src[pos:m.start()])
        out.append(body)
        pos = close + 1
    out.append(src[pos:])
    return wrap_relation("".join(out))


def wrap_relation(src):
    """Breaks a converted line longer than 120 characters before its trigger's relation argument."""
    lines = []
    for line in step11.wrap_long_lines(src).split("\n"):
        cut = line.rfind(", ObjectRelation.")
        if len(line) > 120 and "Triggers." in line and cut > 0:
            indent = len(line) - len(line.lstrip())
            lines.append(line[:cut + 1])
            lines.append(" " * (indent + 8) + line[cut + 2:])
        else:
            lines.append(line)
    return "\n".join(lines)


def main():
    family = FAMILIES[sys.argv[1]]
    slot_re = re.compile(r"EffectSlot\.(%s)\b" % "|".join(family))
    for root_dir in sys.argv[2:]:
        for root, _, files in os.walk(root_dir):
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if not slot_re.search(src):
                    continue
                card = name[:-5]
                if card in DROP_SELF:
                    src = drop_self(src, DROP_SELF[card])
                src = convert_calls(src, card, family)
                src = step11.fix_imports(src)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
