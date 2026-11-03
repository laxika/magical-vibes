#!/usr/bin/env python3
"""Step 14, run 9 of effect-slot-refactor.md: the counter abilities (step 13 family 5, the CounterWatch collectors)
lose their trigger-only wrappers.

- "This ability triggers only once each turn" (OncePerTurnTriggerEffect) is TriggerLimit.ONCE_EACH_TURN, "do this only
  once each turn" (markOnAcceptance) DO_ONLY_ONCE_EACH_TURN, Stalwart Successor's OncePerTurnPerCreatureTriggerEffect
  ONCE_EACH_TURN_FOR_EACH_CREATURE.
- An intervening "if" (a two-argument ConditionalEffect: "during your turn", a chosen mode, "if this card is in your
  graveyard", and the event conditions "when the last time counter is removed") is the option.
- "You put counters" (YouPutCounterOnControlledCreatureTriggerEffect) and "you remove a time counter"
  (YouRemoveTimeCounterFromExiledCardTriggerEffect) are the triggers' events, a TriggeringPermanentConditionalEffect
  the trigger's filter, and TriggerMode.PER_COUNTER the "each counter" event.
- Merged: Ant-Man, Colony Commander's two halves of "whenever you put a +1/+1 counter on a creature", and Ichor
  Aberration's battlefield and graveyard halves of one ability.
- Rules fix: Hardened Bonds triggers on +1/+1 counters only.

The family is small (29 card classes), so the conversion is a table of exact replacements; a replacement that doesn't
match stops the script. Unused imports of the removed wrappers are dropped and the imports the new form needs added.

Usage: python3 scripts/step14-counter-options.py [--dry-run]
"""
import os
import re
import sys

LIMIT = "TriggerOptions.DEFAULT.withLimit(TriggerLimit.%s)"


def once(trigger, effect):
    return "addTrigger(%s,\n                %s,\n                %s);" % (trigger, LIMIT % "ONCE_EACH_TURN", effect)


EDITS = {
    "ContagionDispenser": [(
        """addTrigger(Triggers.youProliferate(),
                new OncePerTurnTriggerEffect(
                        new ConditionalEffect(new ControllerTurn(),
                                new DraftCardFromSpellbookEffect(SPELLBOOK))));""",
        """addTrigger(Triggers.youProliferate(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn())
                        .withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DraftCardFromSpellbookEffect(SPELLBOOK));""")],
    "WatcherOfHours": [(
        """addTrigger(Triggers.timeCounterRemovedFromThisInExile(),
                new YouRemoveTimeCounterFromExiledCardTriggerEffect(new SurveilEffect(1)));""",
        """addTrigger(Triggers.youRemoveTimeCounterFromThisInExile(), new SurveilEffect(1));""")],
    "VeilingOddity": [(
        """addTrigger(Triggers.timeCounterRemovedFromThisInExile(), new ConditionalEffect(
                new NotCondition(new SourceCardSuspended()), new MakeAllCreaturesUnblockableEffect()));""",
        """addTrigger(Triggers.timeCounterRemovedFromThisInExile(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceCardSuspended())),
                new MakeAllCreaturesUnblockableEffect());""")],
    "RiftmarkedKnight": [(
        """addTrigger(Triggers.timeCounterRemovedFromThisInExile(), new ConditionalEffect(
                new NotCondition(new SourceCardSuspended()), new CreateTokenEffect(""",
        """addTrigger(Triggers.timeCounterRemovedFromThisInExile(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceCardSuspended())),
                new CreateTokenEffect("""), (
        """                List.of(StaticAbility.of(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE))))
        )));""",
        """                List.of(StaticAbility.of(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE))))
        ));""")],
    "Terrasymbiosis": [(
        """addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn(),
                OncePerTurnTriggerEffect.markOnAcceptance(
                        new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?")));""",
        """addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?"));""")],
    "TheGreatGoblin": [(
        """.addTrigger(Triggers.countersPutOnCreatureYouControl(),
                new YouPutCounterOnControlledCreatureTriggerEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasAnySubtypePredicate(GOBLIN_ORC_ARMY),
                                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER))));""",
        """.addTrigger(Triggers.youPutCountersOnCreatureYouControl()
                        .withFilter(new PermanentHasAnySubtypePredicate(GOBLIN_ORC_ARMY)),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));""")],
    "StalwartSuccessor": [(
        """addTrigger(Triggers.countersPutOnCreatureYouControl(),
                new OncePerTurnPerCreatureTriggerEffect(new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE)));""",
        """addTrigger(Triggers.countersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE),
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));""")],
    "OmarthisGhostfireInitiate": [(
        """addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsColorlessPredicate(),
                        new MayEffect(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                "Put a +1/+1 counter on Omarthis?")));""",
        """addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(new PermanentIsColorlessPredicate()),
                new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Omarthis?"));""")],
    "MikeyLeoChaosOrder": [(
        """addTrigger(Triggers.countersPutOnCreatureYouControl(),
                new YouPutCounterOnControlledCreatureTriggerEffect(
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))));""",
        once("Triggers.youPutCountersOnCreatureYouControl()", "new DrawCardEffect(1)"))],
    "KnightOfWundagore": [(
        """addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature(),
                new OncePerTurnTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));""",
        once("Triggers.youPutPlusOneCountersOnAnotherCreature()",
             "new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)"))],
    "MossPitSkeleton": [(
        """                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ConditionalEffect(new SourceCardInGraveyard(),
                                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)),
                        "Put Moss-Pit Skeleton on top of your library?"));""",
        """                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new MayEffect(
                        new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0),
                        "Put Moss-Pit Skeleton on top of your library?"));""")],
    "HardenedBonds": [(
        """addTrigger(Triggers.countersPutOnCreatureYouControl(),
                new OncePerTurnTriggerEffect(new SeekLibraryAndPerpetuallyBoostSoughtCardEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, 1)));""",
        """addTrigger(Triggers.plusOneCountersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new SeekLibraryAndPerpetuallyBoostSoughtCardEffect(new CardTypePredicate(CardType.CREATURE), 1, 1));""")],
    "HollowmurkSiege": [(
        """addTrigger(Triggers.countersPutOnCreatureYouControl(), new ConditionalEffect(
                new SourceHasChosenMode("Sultai"),
                new OncePerTurnTriggerEffect(new DrawCardEffect(1))));""",
        """addTrigger(Triggers.countersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode("Sultai"))
                        .withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DrawCardEffect(1));""")],
    "FathomMage": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(), TriggerMode.PER_COUNTER,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));""",
        """addTrigger(Triggers.plusOneCounterPutOnThis(), new MayEffect(new DrawCardEffect(), "Draw a card?"));""")],
    "EarthKingdomGeneral": [(
        """addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                OncePerTurnTriggerEffect.markOnAcceptance(
                        new MayEffect(new GainLifeEffect(new EventValue()), "Gain that much life?")));""",
        """addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(new GainLifeEffect(new EventValue()), "Gain that much life?"));""")],
    "ExemplarOfLight": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(),
                new YouPutCounterOnControlledCreatureTriggerEffect(
                        new OncePerTurnTriggerEffect(new DrawCardEffect())));""",
        once("Triggers.youPutPlusOneCountersOnThis()", "new DrawCardEffect()"))],
    "DuskLegionDuelist": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(),
                new OncePerTurnTriggerEffect(new DrawCardEffect()));""",
        once("Triggers.plusOneCountersPutOnThis()", "new DrawCardEffect()"))],
    "CloakedCadet": [(
        """addTrigger(Triggers.plusOneCountersPutOnHumanYouControl(),
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));""",
        once("Triggers.plusOneCountersPutOnHumanYouControl()", "new DrawCardEffect(1)"))],
    "BlasterCombatDJ": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(),
                new YouPutCounterOnControlledCreatureTriggerEffect(new TransformToBackFaceEffect()));""",
        """addTrigger(Triggers.youPutPlusOneCountersOnThis(), new TransformToBackFaceEffect());""")],
    "BloodcrazedHoplite": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(), TriggerMode.PER_COUNTER,""",
        """addTrigger(Triggers.plusOneCounterPutOnThis(),""")],
    "BrotherhoodScribe": [(
        """addTrigger(Triggers.youGetEnergy(),
                new ConditionalEffect(new ControllerTurn(), new BoostAllOwnCreaturesEffect(1, 1)));""",
        """addTrigger(Triggers.youGetEnergy(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                new BoostAllOwnCreaturesEffect(1, 1));""")],
    "AntManColonyCommander": [(
        """        addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn(),
                new OncePerTurnTriggerEffect(insectToken()));
        addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature(),
                new OncePerTurnTriggerEffect(insectToken()));""",
        """        addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                insectToken());""")],
    "AxgardArtisan": [(
        """addTrigger(Triggers.plusOneCountersPutOnThis(),
                new OncePerTurnTriggerEffect(CreateTokenEffect.ofTreasureToken(1)));""",
        once("Triggers.plusOneCountersPutOnThis()", "CreateTokenEffect.ofTreasureToken(1)"))],
    "DannyPink": [(
        """new TriggeredAbility(Triggers.countersPutOnThis(),
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))),""",
        """new TriggeredAbility(Triggers.countersPutOnThis(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new DrawCardEffect(1)),""")],
    "IchorAberration": [(
        """        CardEffect proliferateEffect = new ConditionalEffect(inGraveyardOrOnBattlefield, SequenceEffect.of(
                new PerpetuallyBoostSourceEffect(1, 1),
                new AllowCastSourceCardFromGraveyardThisTurnEffect()));
        addTrigger(Triggers.youProliferate(), proliferateEffect);
        addTrigger(new TriggerCondition(Triggers.youProliferate(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, proliferateEffect);""",
        """        addTrigger(new TriggerCondition(Triggers.youProliferate(),
                        AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD)),
                TriggerOptions.DEFAULT.withInterveningIf(inGraveyardOrOnBattlefield),
                SequenceEffect.of(
                        new PerpetuallyBoostSourceEffect(1, 1),
                        new AllowCastSourceCardFromGraveyardThisTurnEffect()));""")],
}

VANISHING = (
    """addTrigger(Triggers.timeCountersRemovedFromThis(),
                new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new SacrificeSelfEffect()));""",
    """addTrigger(Triggers.timeCountersRemovedFromThis(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME))),
                new SacrificeSelfEffect());""")
for name in ("RegenerationsRestored", "Calciderm", "Chronozoa", "AvenRiftwatcher"):
    EDITS[name] = [VANISHING]

PKG = "com.github.laxika.magicalvibes"
IMPORTS = {
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
    "Zone": PKG + ".model.Zone",
}
REMOVABLE = ["OncePerTurnTriggerEffect", "ConditionalEffect", "YouPutCounterOnControlledCreatureTriggerEffect",
             "YouRemoveTimeCounterFromExiledCardTriggerEffect", "TriggeringPermanentConditionalEffect",
             "OncePerTurnPerCreatureTriggerEffect", "TriggerMode", "CardEffect", "TriggerCondition",
             "AbilityZones", "Condition"]


def fix_imports(src):
    body = src[src.index("public class"):]
    for name in REMOVABLE:
        if not re.search(r"\b%s\b" % name, body):
            src = re.sub(r"import [\w.]+\.%s;\n" % name, "", src)
    for name, fq in IMPORTS.items():
        if re.search(r"\b%s\b" % name, body) and ("import %s;" % fq) not in src:
            line = "import %s;" % fq
            imports = [i for i in re.findall(r"^import [\w.]+;$", src, re.M) if i.startswith("import com.")]
            before = [i for i in imports if i < line]
            if before:
                at = src.index(before[-1] + "\n") + len(before[-1]) + 1
            else:
                at = src.index(imports[0])
            src = src[:at] + line + "\n" + src[at:]
    return src


def main():
    dry = "--dry-run" in sys.argv
    files = {}
    for root, _, names in os.walk("magical-vibes-card"):
        if "/build/" in root or "/src/main/" not in root + "/":
            continue
        for n in names:
            if n.endswith(".java") and n[:-5] in EDITS:
                files[n[:-5]] = os.path.join(root, n)
    missing = set(EDITS) - set(files)
    if missing:
        sys.exit("card classes not found: %s" % sorted(missing))
    for name, path in sorted(files.items()):
        src = open(path).read()
        new = src
        for old, rep in EDITS[name]:
            if new.count(old) != 1:
                sys.exit("%s: expected one match of:\n%s" % (name, old))
            new = new.replace(old, rep)
        new = fix_imports(new)
        print(path)
        if not dry:
            open(path, "w").write(new)
    print("%d card classes" % len(files), file=sys.stderr)


if __name__ == "__main__":
    main()
