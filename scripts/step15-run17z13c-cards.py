#!/usr/bin/env python3
"""Step 15, runs 17z13c and 17z13d: the cards write their own effects and conditions.

Row 17z13c (collectors that stacked an engine-made effect for the card's marker, or baked event data into it):

- Storm Cauldron: "return it to its owner's hand" is `ReturnToHandEffect.triggering()` (the land-tap default records the
  tapped land as the triggering permanent); `ReturnTappedLandToHandEffect` is gone.
- Relic Bind: its "choose one" is the card's own `ChooseOneEffect`; `RelicBindTapEffect` is gone.
- Hullbreaker Horror: its "choose up to one" is the card's own optional `ChooseOneEffect(options, true, 0, 1)`, each mode
  with its own target filter; `HullbreakerHorrorTriggerEffect` is gone.
- Corrosive Ooze: "destroy all Equipment attached to that creature at end of combat" is
  `DelayedEndOfCombatEffect(DestroyAttachmentsOnTargetCreatureEffect(false, true))`;
  `DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect` is gone.

Row 17z13d (trigger conditions judged by an effect's class):

- Rory Williams: "when you cast this spell from anywhere other than exile" is the ability's event condition;
  `ExileTriggeringSpellWithSuspendCountersEffect.unlessCastFromExile` is gone.
- Reality Smasher: "becomes the target of a spell an opponent controls" is `Triggers.becomesTargetOfOpponentSpell()`
  (the ward collector dropped its discard counter for an ability by the effect's class).

Usage: step15-run17z13c-cards.py LETTER... (the card modules to convert, e.g. `c h r s`). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"


def edit(letter, name, pairs, done_marker):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    src = path.read_text()
    if done_marker in src:
        print("already converted", name)
        return
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("converted", name)


def storm_cauldron():
    edit("s", "StormCauldron", [
        ("import com.github.laxika.magicalvibes.model.effect.ReturnTappedLandToHandEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;\n"),
        ("addTrigger(Triggers.playerTapsLandForMana(), new ReturnTappedLandToHandEffect());",
         "addTrigger(Triggers.playerTapsLandForMana(), ReturnToHandEffect.triggering());"),
    ], "ReturnToHandEffect.triggering()")


def relic_bind():
    edit("r", "RelicBind", [
        ("import com.github.laxika.magicalvibes.model.effect.RelicBindTapEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;\n"),
        ("        addTrigger(Triggers.enchantedPermanentBecomesTapped(), new RelicBindTapEffect());\n",
         "        addTrigger(Triggers.enchantedPermanentBecomesTapped(), new ChooseOneEffect(List.of(\n"
         "                new ChooseOneEffect.ChooseOneOption(\"This Aura deals 1 damage to target player or planeswalker\",\n"
         "                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),\n"
         "                new ChooseOneEffect.ChooseOneOption(\"Target player gains 1 life\",\n"
         "                        new TargetPlayerGainsLifeEffect(1)))));\n"),
    ], "new ChooseOneEffect(")


def hullbreaker_horror():
    edit("h", "HullbreakerHorror", [
        ("import com.github.laxika.magicalvibes.model.effect.HullbreakerHorrorTriggerEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;\n"
         "import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
        ("        addTrigger(Triggers.youCastSpell().anySpell(), new HullbreakerHorrorTriggerEffect());\n",
         "        addTrigger(Triggers.youCastSpell().anySpell(), new ChooseOneEffect(List.of(\n"
         "                new ChooseOneEffect.ChooseOneOption(\"Return target spell you don't control to its owner's hand\",\n"
         "                        new ReturnTargetSpellToHandEffect(),\n"
         "                        new StackEntryPredicateTargetFilter(\n"
         "                                new StackEntryNotPredicate(new StackEntryControlledByPredicate()),\n"
         "                                \"Target must be a spell you don't control.\")),\n"
         "                new ChooseOneEffect.ChooseOneOption(\"Return target nonland permanent to its owner's hand\",\n"
         "                        ReturnToHandEffect.target(),\n"
         "                        new PermanentPredicateTargetFilter(\n"
         "                                new PermanentNotPredicate(new PermanentIsLandPredicate()),\n"
         "                                \"Target must be a nonland permanent.\"))\n"
         "        ), true, 0, 1));\n"),
    ], "new ChooseOneEffect(")


def corrosive_ooze():
    edit("c", "CorrosiveOoze", [
        ("import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;\n"),
        ("        addTrigger(Triggers.blocks().withFilter(equipped),\n"
         "                new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());\n"
         "        addTrigger(Triggers.becomesBlocked().withFilter(equipped),\n"
         "                TriggerOptions.EACH_BLOCKER,\n"
         "                new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());\n",
         "        addTrigger(Triggers.blocks().withFilter(equipped),\n"
         "                new DelayedEndOfCombatEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true)));\n"
         "        addTrigger(Triggers.becomesBlocked().withFilter(equipped),\n"
         "                TriggerOptions.EACH_BLOCKER,\n"
         "                new DelayedEndOfCombatEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true)));\n"),
    ], "DelayedEndOfCombatEffect(")


def rory_williams():
    edit("r", "RoryWilliams", [
        ("import com.github.laxika.magicalvibes.model.Card;\n",
         "import com.github.laxika.magicalvibes.model.Card;\n"
         "import com.github.laxika.magicalvibes.model.Zone;\n"
         "import com.github.laxika.magicalvibes.model.condition.CastFromZone;\n"
         "import com.github.laxika.magicalvibes.model.condition.NotCondition;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
         "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"),
        ("        addTrigger(Triggers.youCastThisSpell(),\n"
         "                ExileTriggeringSpellWithSuspendCountersEffect.unlessCastFromExile(\n"
         "                        3, CreateTokenEffect.ofClueToken(1)));\n",
         "        // \"When you cast this spell from anywhere other than exile\" is the trigger's event condition (CR 603.2).\n"
         "        addTrigger(Triggers.youCastThisSpell(),\n"
         "                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new CastFromZone(Zone.EXILE))),\n"
         "                new ExileTriggeringSpellWithSuspendCountersEffect(3, CreateTokenEffect.ofClueToken(1)));\n"),
    ], "new NotCondition(new CastFromZone(Zone.EXILE))")


def reality_smasher():
    edit("r", "RealitySmasher", [
        ("addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessDiscardsEffect());",
         "addTrigger(Triggers.becomesTargetOfOpponentSpell(), new CounterUnlessDiscardsEffect());"),
    ], "Triggers.becomesTargetOfOpponentSpell()")


BY_LETTER = {
    "c": [corrosive_ooze],
    "h": [hullbreaker_horror],
    "r": [relic_bind, rory_williams, reality_smasher],
    "s": [storm_cauldron],
}

if __name__ == "__main__":
    letters = sys.argv[1:]
    if not letters:
        sys.exit("usage: step15-run17z13c-cards.py LETTER...")
    for letter in letters:
        for convert in BY_LETTER.get(letter, []):
            convert()
