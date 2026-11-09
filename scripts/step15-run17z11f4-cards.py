#!/usr/bin/env python3
"""Step 15, run 17z11f4: the cards whose abilities are written as printed instead of read by an effect's class.

Idempotent; per letter module. Optional repository-root argument (default: this script's repository).
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def card(letter, name):
    return CARDS / letter / "src/main/java/com/github/laxika/magicalvibes/cards" / letter / f"{name}.java"


def edit(path, pairs, imports_add=(), imports_remove=()):
    s = path.read_text()
    orig = s
    for old, new in pairs:
        if new in s and old not in s:
            continue
        if old not in s:
            raise SystemExit(f"{path.name}: missing {old!r}")
        s = s.replace(old, new)
    for imp in imports_remove:
        line = f"import {imp};\n"
        s = s.replace(line, "")
    for imp in imports_add:
        line = f"import {imp};\n"
        if line not in s:
            first = s.index("import ")
            s = s[:first] + line + s[first:]
    if s != orig:
        path.write_text(s)
        print("edited", path.name)


EDITS = []


def main():
    for args in EDITS:
        edit(*args)


# Minn, Wily Illusionist: "mana value less than or equal to that creature's power", its actual last-known power.
EDITS.append((card("m", "MinnWilyIllusionist"),
              [("new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), \"permanent\")\n"
                "                        .boundedByEventValue();",
                "new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), \"permanent\")\n"
                "                        .boundedBy(new TriggeringPermanentPower(true));")],
              ["com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower"]))

# Havengul Mystery, Zenos yae Galvus: "when the chosen creature leaves the battlefield, transform this" is a trigger
# filter on the leaving permanent, judged as it leaves (CR 603.10a), and a plain transform.
for letter, name in (("h", "HavengulMystery"), ("z", "ZenosYaeGalvus")):
    EDITS.append((card(letter, name),
                  [("addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),\n"
                    "                new TransformSelfWhenChosenPermanentLeavesEffect());",
                    "addTrigger(Triggers.leavesBattlefield(new PermanentIsChosenPermanentPredicate(),\n"
                    "                        ObjectRelation.ANOTHER),\n"
                    "                new TransformSelfEffect());")],
                  ["com.github.laxika.magicalvibes.model.effect.TransformSelfEffect",
                   "com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate"],
                  ["com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect"]))

# The clash cards: a "whenever you clash" ability's event value is whether you won (1) or not (0). "Whenever you clash
# and win" / "whenever you win a clash" is the event condition; "if you won" an "if" read as the ability resolves.
WON = "new EventValueAtLeast(1)"
CLASH_IMPORTS = ["com.github.laxika.magicalvibes.model.condition.EventValueAtLeast"]
EDITS.append((card("s", "SylvanEchoes"),
              [("        // Whenever you clash and win, you may draw a card.\n"
                "        //\n"
                "        // Non-targeting clash trigger: TriggerCollectionService.fireClashTriggers keeps the\n"
                "        // IfWonClashEffect branch only when the controller won and pushes the wrapped effect\n"
                "        // straight onto the stack.\n"
                "        addTrigger(Triggers.youClash(), new IfWonClashEffect(\n"
                "                new MayEffect(new DrawCardEffect(1), \"Draw a card?\")));",
                "        addTrigger(Triggers.youClash(), TriggerOptions.DEFAULT.withEventCondition(" + WON + "),\n"
                "                new MayEffect(new DrawCardEffect(1), \"Draw a card?\"));")],
              CLASH_IMPORTS + ["com.github.laxika.magicalvibes.model.trigger.TriggerOptions"],
              ["com.github.laxika.magicalvibes.model.effect.IfWonClashEffect"]))
EDITS.append((card("m", "MarvoDeepOperative"),
              [("        addTrigger(Triggers.youClash(), new IfWonClashEffect(SequenceEffect.of(\n"
                "                new DrawCardEffect(1),\n"
                "                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(\n"
                "                        new CardMaxManaValuePredicate(8)))));",
                "        addTrigger(Triggers.youClash(), TriggerOptions.DEFAULT.withEventCondition(" + WON + "),\n"
                "                SequenceEffect.of(\n"
                "                        new DrawCardEffect(1),\n"
                "                        new MayCastAnySpellFromHandWithoutPayingManaCostEffect(\n"
                "                                new CardMaxManaValuePredicate(8))));")],
              CLASH_IMPORTS + ["com.github.laxika.magicalvibes.model.trigger.TriggerOptions"],
              ["com.github.laxika.magicalvibes.model.effect.IfWonClashEffect"]))
EDITS.append((card("e", "EntanglingTrap"),
              [("        // Whenever you clash, tap target creature an opponent controls. If you won, that creature\n"
                "        // doesn't untap during its controller's next untap step. (Fired after the clash ends by\n"
                "        // TriggerCollectionService.performClash; the target is chosen via the ClashTriggerTarget\n"
                "        // interaction. The IfWonClashEffect clause is applied only on a won clash.)\n"
                "        addTrigger(Triggers.youClash(), new TapPermanentsEffect(TapUntapScope.TARGET),\n"
                "                new IfWonClashEffect(new SkipNextUntapEffect(TapUntapScope.TARGET)));",
                "        addTrigger(Triggers.youClash(), new TapPermanentsEffect(TapUntapScope.TARGET),\n"
                "                new ConditionalEffect(" + WON + ", new SkipNextUntapEffect(TapUntapScope.TARGET)));")],
              CLASH_IMPORTS + ["com.github.laxika.magicalvibes.model.effect.ConditionalEffect"],
              ["com.github.laxika.magicalvibes.model.effect.IfWonClashEffect"]))
EDITS.append((card("r", "RebellionOfTheFlamekin"),
              [("        // Whenever you clash, you may pay {1}. If you do, create a 3/1 red Elemental Shaman creature\n"
                "        // token. If you won, that token gains haste until end of turn.\n"
                "        //\n"
                "        // The token is created either way, so both branches offer the same \"pay {1}\" may-pay; only\n"
                "        // the granted haste differs. Since the clash winner is fixed the moment the trigger fires,\n"
                "        // TriggerCollectionService.fireClashTriggers picks exactly one branch: the IfWonClashEffect\n"
                "        // (haste) variant on a win, the IfLostClashEffect (no haste) variant otherwise. Non-targeting,\n"
                "        // so it goes straight onto the stack rather than the ClashTriggerTarget interaction.\n"
                "        addTrigger(Triggers.youClash(), new IfWonClashEffect(\n"
                "                new MayPayManaEffect(\"{1}\", createTokenEffect(Set.of(Keyword.HASTE)),\n"
                "                        \"Pay {1} to create a 3/1 red Elemental Shaman with haste?\")),\n"
                "                new IfLostClashEffect(\n"
                "                        new MayPayManaEffect(\"{1}\", createTokenEffect(Set.of()),\n"
                "                                \"Pay {1} to create a 3/1 red Elemental Shaman?\")));",
                "        // The clash's outcome is the ability's event value, so exactly one \"if\" holds as it resolves.\n"
                "        addTrigger(Triggers.youClash(), new ConditionalEffect(" + WON + ",\n"
                "                        new MayPayManaEffect(\"{1}\", createTokenEffect(Set.of(Keyword.HASTE)),\n"
                "                                \"Pay {1} to create a 3/1 red Elemental Shaman with haste?\")),\n"
                "                new ConditionalEffect(new NotCondition(" + WON + "),\n"
                "                        new MayPayManaEffect(\"{1}\", createTokenEffect(Set.of()),\n"
                "                                \"Pay {1} to create a 3/1 red Elemental Shaman?\")));")],
              CLASH_IMPORTS + ["com.github.laxika.magicalvibes.model.condition.NotCondition",
                               "com.github.laxika.magicalvibes.model.effect.ConditionalEffect"],
              ["com.github.laxika.magicalvibes.model.effect.IfWonClashEffect",
               "com.github.laxika.magicalvibes.model.effect.IfLostClashEffect"]))

# Liliana, Defiant Necromancer: the emblem's "whenever a creature dies" is a triggered ability of the emblem.
EDITS.append((card("l", "LilianaDefiantNecromancer"),
              [("                List.of(new CreateEmblemEffect(\n"
                "                        List.of(new RegisterDelayedReturnDyingCreatureUnderControlEffect(\n"
                "                                false, null, 0, null, null)),\n"
                "                        \"Whenever a creature dies, return it to the battlefield under your control at \"\n"
                "                                + \"the beginning of the next end step.\")),",
                "                List.of(new CreateEmblemEffect(\n"
                "                        new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY),\n"
                "                                new RegisterDelayedReturnDyingCreatureUnderControlEffect(\n"
                "                                        false, null, 0, null, null)),\n"
                "                        \"Whenever a creature dies, return it to the battlefield under your control at \"\n"
                "                                + \"the beginning of the next end step.\")),")],
              ["com.github.laxika.magicalvibes.model.TriggeredAbility",
               "com.github.laxika.magicalvibes.model.trigger.ObjectRelation",
               "com.github.laxika.magicalvibes.model.trigger.Triggers"]))

# Noble Purpose: "whenever a creature you control deals combat damage, you gain that much life" is a triggered ability.
EDITS.append((card("n", "NoblePurpose"),
              [("        // Whenever a creature you control deals combat damage, you gain that much life.\n"
                "        addStaticEffect(new GainLifeEqualToControlledCreatureCombatDamageEffect());",
                "        addTrigger(Triggers.creatureYouControlDealsCombatDamage(), new GainLifeEffect(new EventValue()));")],
              ["com.github.laxika.magicalvibes.model.amount.EventValue",
               "com.github.laxika.magicalvibes.model.effect.GainLifeEffect",
               "com.github.laxika.magicalvibes.model.trigger.Triggers"],
              ["com.github.laxika.magicalvibes.model.effect.GainLifeEqualToControlledCreatureCombatDamageEffect"]))

# Kaito Shizuki: the emblem's "whenever a creature you control deals combat damage to a player" is a triggered ability.
EDITS.append((card("k", "KaitoShizuki"),
              [("                List.of(new CreateEmblemEffect(\n"
                "                        List.of(new SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect.Marker()),\n",
                "                List.of(new CreateEmblemEffect(\n"
                "                        new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(),\n"
                "                                new SearchLibraryEffect(new CardAllOfPredicate(List.of(\n"
                "                                        new CardTypePredicate(CardType.CREATURE),\n"
                "                                        new CardAnyOfPredicate(List.of(\n"
                "                                                new CardColorPredicate(CardColor.BLUE),\n"
                "                                                new CardColorPredicate(CardColor.BLACK))))),\n"
                "                                        LibrarySearchDestination.BATTLEFIELD)),\n")],
              ["com.github.laxika.magicalvibes.model.TriggeredAbility",
               "com.github.laxika.magicalvibes.model.CardType",
               "com.github.laxika.magicalvibes.model.LibrarySearchDestination",
               "com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect",
               "com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate",
               "com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate",
               "com.github.laxika.magicalvibes.model.filter.CardColorPredicate",
               "com.github.laxika.magicalvibes.model.filter.CardTypePredicate"],
              ["com.github.laxika.magicalvibes.model.effect.SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect"]))

if __name__ == "__main__":
    main()
