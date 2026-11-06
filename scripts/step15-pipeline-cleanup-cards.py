#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 17a: no "you may" reaches a trigger handler by being unwrapped.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-pipeline-cleanup-cards.py "$m"; done

Each card below wrapped an effect class that a collector handler, keyed by that class, found only by unwrapping the
card's MayEffect, then judged the trigger condition and rebuilt the effect with the event bound in. Each is now
written with a trigger filter and existing effects that read the event from the stack entry, so the generic
collectors put the ability on the stack with its "you may" (CR 603.5):

- Bounteous Kirin, Cloudhoof Kirin: "a Spirit or Arcane spell" is the cast trigger's filter; "that spell's mana
  value" is TargetSpellManaValue.
- Mishra, Artificer Prodigy: "an artifact spell" is the filter; the search names the triggering spell.
- Mold Adder: "a blue or black spell" is the filter; the counter is PutCountersOnSourceEffect.
- Pontiff of Blight: extort is written as every printed extort card writes it (a "you may pay {W/B}" drain).
- Mana Echoes: "Whenever a creature enters" is the creature enters trigger; the effect reads the creature.
- Screeching Scorchbeast: "create that many tokens" is the plain CreateTokenEffect of the event value.
"""
import pathlib
import sys

SPIRIT_OR_ARCANE = (
    "new CardAnyOfPredicate(List.of(\n"
    "                        new CardSubtypePredicate(CardSubtype.SPIRIT),\n"
    "                        new CardSubtypePredicate(CardSubtype.ARCANE)))"
)

CARDS = {
    "BounteousKirin.java": [
        ("import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToCastSpellManaValueEffect;\n",
         "import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;\n"
         "import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;\n"),
        ("        addTrigger(Triggers.youCastSpell(), new MayEffect(\n"
         "                new GainLifeEqualToCastSpellManaValueEffect(" + SPIRIT_OR_ARCANE + "),\n",
         "        addTrigger(Triggers.youCastSpell().withFilter(" + SPIRIT_OR_ARCANE + "),\n"
         "                new MayEffect(new GainLifeEffect(new TargetSpellManaValue()),\n"),
    ],
    "CloudhoofKirin.java": [
        ("import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByCastSpellManaValueEffect;\n",
         "import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;\n"
         "import com.github.laxika.magicalvibes.model.effect.MillEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.MillRecipient;\n"),
        ("        addTrigger(Triggers.youCastSpell(), new MayEffect(\n"
         "                new MillTargetPlayerByCastSpellManaValueEffect(" + SPIRIT_OR_ARCANE + "),\n",
         "        addTrigger(Triggers.youCastSpell().withFilter(" + SPIRIT_OR_ARCANE + "),\n"
         "                new MayEffect(new MillEffect(new TargetSpellManaValue(), MillRecipient.TARGET_PLAYER),\n"),
    ],
    "MishraArtificerProdigy.java": [
        ("import com.github.laxika.magicalvibes.model.Card;\n",
         "import com.github.laxika.magicalvibes.model.Card;\n"
         "import com.github.laxika.magicalvibes.model.CardType;\n"),
        ("import com.github.laxika.magicalvibes.model.effect.SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n"),
        ("        addTrigger(Triggers.youCastSpell(), new MayEffect(\n"
         "                new SearchSameNameCardToBattlefieldOnArtifactSpellCastEffect(),\n",
         "        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)), new MayEffect(\n"
         "                SearchZonesForCardNamedToBattlefieldEffect.namedAsTriggeringSpell(),\n"),
    ],
    "MoldAdder.java": [
        ("import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;\n"),
        ("import java.util.Set;\n", "import java.util.List;\n"),
        ("        addTrigger(Triggers.opponentCastsSpell(),\n"
         "                new MayEffect(\n"
         "                        new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(\n"
         "                                Set.of(CardColor.BLUE, CardColor.BLACK),\n"
         "                                1,\n"
         "                                false\n"
         "                        ),\n",
         "        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardAnyOfPredicate(List.of(\n"
         "                        new CardColorPredicate(CardColor.BLUE),\n"
         "                        new CardColorPredicate(CardColor.BLACK)))),\n"
         "                new MayEffect(\n"
         "                        new PutCountersOnSourceEffect(1, 1, 1),\n"),
    ],
    "PontiffOfBlight.java": [
        ("import com.github.laxika.magicalvibes.model.effect.MayEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
        ("        addTrigger(Triggers.youCastSpell(), extort());\n",
         "        addTrigger(Triggers.youCastSpell().anySpell(), extort());\n"),
        ("                new TriggeredAbility(Triggers.youCastSpell(), extort()), GrantScope.OWN_CREATURES));\n",
         "                new TriggeredAbility(Triggers.youCastSpell().anySpell(), extort()), GrantScope.OWN_CREATURES));\n"),
        ("    private static MayEffect extort() {\n"
         "        return new MayEffect(\n"
         "                new SpellCastTriggerEffect(\n"
         "                        null,\n"
         "                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),\n"
         "                        \"{W/B}\"\n"
         "                ),\n"
         "                \"Pay {W/B} to extort?\"\n"
         "        );\n",
         "    private static MayPayManaEffect extort() {\n"
         "        return new MayPayManaEffect(\"{W/B}\",\n"
         "                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true), \"Pay {W/B} to extort?\");\n"),
    ],
    "ManaEchoes.java": [
        ("        addTrigger(Triggers.enters(null, ObjectRelation.ANY),\n",
         "        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),\n"),
    ],
    "ScreechingScorchbeast.java": [
        ("import com.github.laxika.magicalvibes.model.effect.CreateTokensForNonlandCardsMilledEffect;\n", ""),
        ("                        new CreateTokensForNonlandCardsMilledEffect(zombieMutant),\n",
         "                        zombieMutant,\n"),
    ],
}


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new and new in converted:
            continue
        if old in converted:
            converted = converted.replace(old, new)
        elif new:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main():
    module = pathlib.Path(sys.argv[1])
    for path in sorted(module.glob("src/main/java/**/*.java")):
        if path.name in CARDS:
            convert(path, CARDS[path.name])


if __name__ == "__main__":
    main()
