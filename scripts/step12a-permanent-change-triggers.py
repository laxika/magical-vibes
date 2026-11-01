#!/usr/bin/env python3
"""Moves card classes off the phasing, transform and turned-face-up EffectSlots (effect-slot-refactor.md step 12a,
part 2). These slots move with the zone-change family although none of them is a zone change (CR 702.26d).

* Every registration becomes a triggered ability with a real PermanentChangeTrigger. The triggers are the keys of
  the slot's ZoneChangeWatch in the engine, so the two must stay the same. Each slot keeps the relation its
  collector gave it.
* The "as this is turned face up" replacements the turned-face-up slot also held (megamorph counters, Vesuvan
  Shapeshifter's copy, the Aura attachment, the power/toughness form choice; CR 614.1e) become static abilities, as
  step 8 did for "as this enters".
* "As this transforms into [this face]" (Sephiroth's emblem, Olag's copy) becomes a static AsTransformsIntoThisEffect.
* The registrations the old collectors put on the stack as one object ("when this is turned face up, A and B",
  "when this transforms, A and B") become one ability: MANUAL rewrites those cards by exact replacement.

Usage: python3 scripts/step12a-permanent-change-triggers.py DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step12a = __import__("step12a-zone-change-triggers")
step11 = step12a.step11
step11.IMPORTS["new AsTransformsIntoThisEffect("] = \
    "com.github.laxika.magicalvibes.model.effect.AsTransformsIntoThisEffect"
step11.IMPORTS["StaticAbility.of("] = "com.github.laxika.magicalvibes.model.StaticAbility"

FAMILY = {
    "ON_SELF_PHASES_OUT": ("Triggers.phasesOut(null, ObjectRelation.{rel})", "THIS", None),
    "ON_ANY_OTHER_PERMANENT_PHASES_OUT": ("Triggers.phasesOut(null, ObjectRelation.{rel})", "ANOTHER", None,
                                          "ONE_OR_MORE"),
    "ON_SELF_PHASES_IN": ("Triggers.phasesIn(null, ObjectRelation.{rel})", "THIS", None),
    "ON_TRANSFORM_TO_BACK_FACE": ("Triggers.transformsToBackFace(ObjectRelation.{rel})", "THIS", None),
    "ON_TRANSFORM_TO_FRONT_FACE": ("Triggers.transformsToFrontFace(ObjectRelation.{rel})", "THIS", None),
    "ON_EQUIPPED_CREATURE_TRANSFORMS": ("Triggers.transforms(null, ObjectRelation.{rel})", "EQUIPPED", None),
    "ON_ALLY_PERMANENT_TRANSFORMS": ("Triggers.transforms(null, ObjectRelation.{rel})", "YOU_CONTROL", None),
    "ON_TURNED_FACE_UP": ("Triggers.turnedFaceUp(null, ObjectRelation.{rel})", "THIS", None),
    "ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP": ("Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.{rel})",
                                               "YOU_CONTROL", None),
    "ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP": ("Triggers.turnedFaceUp(null, ObjectRelation.{rel})", "ANY", None),
    "ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP": ("Triggers.turnedFaceUp(null, ObjectRelation.{rel})", "YOU_CONTROL",
                                                None),
    "GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP": ("Triggers.turnedFaceUp(null, ObjectRelation.{rel})",
                                                  "YOU_CONTROL", "GRAVEYARD"),
}

FACE_UP_REPLACEMENTS = r"(?:new (?:PutCountersOnTurnFaceUpEffect|TurnFaceUpAttachAuraEffect|TurnFaceUpCopyEffect|" \
                       r"PowerToughnessFormChoiceEffect)\b|turnFaceUpCopy\b)"
AS_TRANSFORMS = r"new (?:CreateEmblemEffect|ApplyLudevicCopyEffect)\b"

FACE_UP = "Triggers.turnedFaceUp(null, ObjectRelation.THIS)"
TO_BACK = "Triggers.transformsToBackFace(ObjectRelation.THIS)"
MANUAL = {
    "RiptideSurvivor": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DrawCardEffect(3));""",
        """        addTrigger(%s, new DiscardEffect(2, DiscardRecipient.CONTROLLER),
                new DrawCardEffect(3));""" % FACE_UP)],
    "RattleclawMystic": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.GREEN));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.BLUE));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardManaEffect(ManaColor.RED));""",
        """        addTrigger(%s, new AwardManaEffect(ManaColor.GREEN),
                new AwardManaEffect(ManaColor.BLUE), new AwardManaEffect(ManaColor.RED));""" % FACE_UP)],
    "MaelstromDjinn": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new PutCountersOnSelfEffect(CounterType.TIME, 2));
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new GrantKeywordEffect(Keyword.VANISHING, GrantScope.SELF, GrantDuration.INDEFINITE));
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new GrantEffectToSourceEffect(""",
        """        addTrigger(%s,
                new PutCountersOnSelfEffect(CounterType.TIME, 2),
                new GrantKeywordEffect(Keyword.VANISHING, GrantScope.SELF, GrantDuration.INDEFINITE),
                new GrantEffectToSourceEffect(""" % FACE_UP)],
    "GreenbeltRadical": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));""",
        """        addTrigger(%s,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));""" % FACE_UP)],
    "EssenceOfAntiquity": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));""",
        """        addTrigger(%s,
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));"""
        % FACE_UP)],
    # One ability whose two effects use two target groups: each effect is bound to its group, then added once.
    "CovetedFalcon": [(
        """        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_TURNED_FACE_UP, new TargetGroupMarkerEffect());
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you control"
        ), 0, Integer.MAX_VALUE).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect(2));""",
        """        TargetGroupMarkerEffect opponent = new TargetGroupMarkerEffect();
        TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect gainControl =
                new TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect(2);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(opponent);
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you control"
        ), 0, Integer.MAX_VALUE).bindEffect(gainControl);
        addTrigger(%s, opponent, gainControl);""" % FACE_UP)],
    "AlleyAssailant": [(
        """        )).addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new GainLifeEffect(3));""",
        """        )).addTrigger(%s,
                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(3));""" % FACE_UP)],
    # A replacement written with its fully qualified class name, and one held in a variable that is already a static
    # ability for "as this enters": both are static abilities only.
    "AcidSpewerDragon": [(
        """        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, false));""",
        """        addStaticEffect(new com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, false));""")],
    "AquamorphEntity": [("""        addEffect(EffectSlot.ON_TURNED_FACE_UP, formChoice);\n""", "")],
    "CasalPathbreakerOwlbear": [(
        """        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new BoostAllOwnCreaturesEffect(2, 2, otherLegendary));
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, legendary));""",
        """        addTrigger(%s,
                new BoostAllOwnCreaturesEffect(2, 2, otherLegendary),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, legendary));""" % TO_BACK)],
    "AvacynThePurifier": [(
        """        addSimultaneousEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,""",
        """        addTrigger(%s,""" % TO_BACK)],
    "HuntmasterOfTheFells": [(
        """        addWolfAndLifeTrigger(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE);""",
        """        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), wolfToken(), new GainLifeEffect(2));"""),
        ("""    private void addWolfAndLifeTrigger(EffectSlot slot) {
        addEffect(slot, new CreateTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        ));
        addEffect(slot, new GainLifeEffect(2));
    }""",
         """    private void addWolfAndLifeTrigger(EffectSlot slot) {
        addEffect(slot, wolfToken());
        addEffect(slot, new GainLifeEffect(2));
    }

    private static CreateTokenEffect wolfToken() {
        return new CreateTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        );
    }""")],
}


def statics(src):
    """Turns the "as this is turned face up" and "as this transforms into" registrations into static abilities."""
    src = re.sub(r"\baddEffect\(EffectSlot\.ON_TURNED_FACE_UP,(\s*)(?=%s)" % FACE_UP_REPLACEMENTS,
                 r"addStaticEffect(", src)
    src = re.sub(r"LegacySlots\.triggered\(EffectSlot\.ON_TURNED_FACE_UP, List\.of\((turnFaceUpCopy)\)\)",
                 r"StaticAbility.of(\1)", src)
    pattern = re.compile(r"\baddEffect\(EffectSlot\.ON_TRANSFORM_TO_BACK_FACE,\s*(?=%s)" % AS_TRANSFORMS)
    out, pos = [], 0
    for m in pattern.finditer(src):
        close = step11.find_close(src, src.index("(", m.start()))
        out.append(src[pos:m.start()])
        out.append("addStaticEffect(new AsTransformsIntoThisEffect(%s))" % src[m.end():close])
        pos = close + 1
    out.append(src[pos:])
    return "".join(out)


def main():
    slot_re = re.compile(r"EffectSlot\.(%s)\b" % "|".join(FAMILY))
    for root_dir in sys.argv[1:]:
        for root, _, files in os.walk(root_dir):
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if not slot_re.search(src):
                    continue
                card = name[:-5]
                for old, new in MANUAL.get(card, []):
                    if old not in src:
                        raise ValueError("%s: manual text not found" % card)
                    src = src.replace(old, new)
                src = statics(src)
                src = step12a.convert_calls(src, card, FAMILY)
                src = step11.fix_imports(src)
                if slot_re.search(src):
                    raise ValueError("%s: a registration is left" % card)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
