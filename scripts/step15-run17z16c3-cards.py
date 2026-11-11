#!/usr/bin/env python3
"""Step 15, run 17z16c3: the cards whose reflexive ability took the event's value as its X read the event.

`QueueReflexiveAbilityEffect` no longer has `useEventValueAsX`: a reflexive ability records the resolving ability's
X and its event value (`ReflexiveAbilitySupport`), and an effect that means "that much", "that many" or a value the
preceding action determined reads the event value. `PutCountersOnTargetPermanentThenReflexiveEffect` lost its
`reflexiveXValue` (no card set it). The continuation whose "that creature" is the resolving entry's target is
`QueueReflexiveAbilityEffect.aboutEntryTarget(effect)`.

Oracle text checked with the Scryfall MCP:

A restriction or amount written with X that is the event's value:

- Unleash the Inferno (SNC 229): "Unleash the Inferno deals 7 damage to target creature or planeswalker. When it
  deals excess damage this way, destroy target artifact or enchantment an opponent controls with mana value less
  than or equal to that amount of excess damage." `PermanentMaxManaValueXPredicate` ->
  `PermanentManaValueAtMostEventValuePredicate`.
- Saruman of Many Colors (HOC 35): "Whenever you cast your second spell each turn, each opponent mills two cards.
  When one or more cards are milled this way, exile target enchantment, instant, or sorcery card with equal or
  lesser mana value than that spell from an opponent's graveyard. ..." The cast collector records the spell's mana
  value as the entry's event value: `CardMaxManaValueXPredicate` -> `CardManaValueAtMostEventValuePredicate`.
- Ancient Brass Dragon (HBG 141): "Whenever this creature deals combat damage to a player, roll a d20. When you do,
  put any number of target creature cards with total mana value X or less from graveyards onto the battlefield
  under your control, where X is the result." The ability defines X as the result (CR 107.3), which the roll records
  as the event value: `XValue` -> `EventValue`.

The flag dropped, the effects already read the event value:

- Ancient Bronze Dragon (HBG 199): "... roll a d20. When you do, put X +1/+1 counters on each of up to two target
  creatures, where X is the result."
- Yuna's Whistle (FIC 75): "... When you reveal a creature card this way, put X +1/+1 counters on target creature
  you control, where X is the mana value of that card."
- Swashbuckler Extraordinaire (HBG 190): "Whenever you attack, you may sacrifice one or more Treasures. When you
  do, up to that many target creatures gain double strike until end of turn."
- Ravenous Rotbelly (SLD 849): "... you may sacrifice up to three Zombies. When you sacrifice one or more Zombies
  this way, each opponent sacrifices that many creatures of their choice."
- Rose Room Treasurer (NCC 51): "... Otherwise, you may pay {X}. When you do, this creature deals X damage to any
  target." (the X paid is recorded as the event value by `PayXManaEffect`)
- Nyssa of Traken (WHO 51): "... sacrifice any number of artifacts. When you sacrifice one or more artifacts this
  way, tap up to that many target creatures and draw that many cards."
- Campsite Cuisine (FIC 464): "Whenever you attack, you may sacrifice X Foods. When you do, up to X target attacking
  creatures each get +3/+3 and gain trample and indestructible until end of turn." (the number sacrificed)
- Incinerator of the Guilty (MKM 132): "... you may collect evidence X. When you do, this creature deals X damage to
  each creature and planeswalker that player controls." (the X chosen is recorded as the event value, which
  `DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect` reads)

Constructor spelling only:

- Ancestral Katana (NEO 1), Anointer of Valor (2X2 6): `aboutEntryTarget`.
- Dimension X Pizzasaur (TMC 16), Spectacular Showdown (MKC 162): no `reflexiveXValue` argument.

One card test is added (`TESTS`), for the rules fix of `PayXManaPutCountersOnSelfEffectHandler`:

- Wildborn Preserver (ELD 182): "Whenever another non-Human creature you control enters, you may pay {X}. When you
  do, put X +1/+1 counters on this creature." The reflexive ability has no target, so it puts its counters on a
  Wildborn Preserver that has shroud; its entry named the creature as a target and was removed on resolution.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c3-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

EDITS = {
    "UnleashTheInferno": ("u", [
        ("new PermanentMaxManaValueXPredicate()));", "new PermanentManaValueAtMostEventValuePredicate()));"),
        ("new DestroyTargetPermanentEffect(artifactOrEnchantmentOpponentControls), false, true)));",
         "new DestroyTargetPermanentEffect(artifactOrEnchantmentOpponentControls))));"),
        ("import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostEventValuePredicate;\n"),
    ]),
    "SarumanOfManyColors": ("s", [
        ("new CardAllOfPredicate(List.of(noncreatureSpell, new CardMaxManaValueXPredicate())),",
         "new CardAllOfPredicate(List.of(noncreatureSpell,\n"
         "                                                new CardManaValueAtMostEventValuePredicate())),"),
        ("GraveyardSearchScope.OPPONENT_GRAVEYARD),\n                                false, true)));",
         "GraveyardSearchScope.OPPONENT_GRAVEYARD))));"),
        ("import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostEventValuePredicate;\n"),
    ]),
    "AncientBrassDragon": ("a", [
        ("null, 0, new XValue(), null, null, null, 0,", "null, 0, new EventValue(), null, null, null, 0,"),
        ("new QueueReflexiveAbilityEffect(reanimateCreatures, false, true);",
         "new QueueReflexiveAbilityEffect(reanimateCreatures);"),
        ("import com.github.laxika.magicalvibes.model.amount.XValue;\n",
         "import com.github.laxika.magicalvibes.model.amount.EventValue;\n"),
    ]),
    "AncientBronzeDragon": ("a", [
        ("new QueueReflexiveAbilityEffect(putCounters, false, true);",
         "new QueueReflexiveAbilityEffect(putCounters);"),
    ]),
    "YunasWhistle": ("y", [
        ("                                false, null),\n                        false, true))));",
         "                                false, null)))));"),
    ]),
    "SwashbucklerExtraordinaire": ("s", [
        ("new QueueReflexiveAbilityEffect(grantDoubleStrike, false, true))),",
         "new QueueReflexiveAbilityEffect(grantDoubleStrike))),"),
    ]),
    "RavenousRotbelly": ("r", [
        ("SacrificeRecipient.EACH_OPPONENT),\n                                        false,\n"
         "                                        true))),",
         "SacrificeRecipient.EACH_OPPONENT)))),"),
    ]),
    "RoseRoomTreasurer": ("r", [
        ("new DealDamageToAnyTargetEffect(new EventValue()),\n"
         "                                                false,\n"
         "                                                true)))));",
         "new DealDamageToAnyTargetEffect(new EventValue()))))));"),
    ]),
    "NyssaOfTraken": ("n", [
        ("new DrawCardEffect(new EventValue())),\n                                        false,\n"
         "                                        true))));",
         "new DrawCardEffect(new EventValue()))))));"),
    ]),
    "CampsiteCuisine": ("c", [
        ("grantKeywords),\n                                                false,\n"
         "                                                true))),",
         "grantKeywords)))),"),
    ]),
    "IncineratorOfTheGuilty": ("i", [
        ("new DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect(),\n"
         "                                false, true)),",
         "new DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect())),"),
    ]),
    "AncestralKatana": ("a", [
        ("new QueueReflexiveAbilityEffect(\n"
         "                                AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),\n"
         "                                false, false, true),",
         "QueueReflexiveAbilityEffect.aboutEntryTarget(\n"
         "                                AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()),"),
    ]),
    "AnointerOfValor": ("a", [
        ("new QueueReflexiveAbilityEffect(new PutCounterOnEachMatchingPermanentEffect(",
         "QueueReflexiveAbilityEffect.aboutEntryTarget(new PutCounterOnEachMatchingPermanentEffect("),
        ("EachPermanentScope.ALL_PLAYERS), false, false, true),", "EachPermanentScope.ALL_PLAYERS)),"),
    ]),
    "DimensionXPizzasaur": ("d", [
        ("                        2,\n                        null,\n"
         "                        new DestroyTargetPermanentEffect(eligibleDestructionTarget),",
         "                        2,\n"
         "                        new DestroyTargetPermanentEffect(eligibleDestructionTarget),"),
    ]),
    "SpectacularShowdown": ("s", [
        ("CounterType.DOUBLE_STRIKE, 1, null,\n"
         "                        new GoadTriggeringCreatureUntilNextTurnEffect(), false, null, true),",
         "CounterType.DOUBLE_STRIKE, 1,\n"
         "                        new GoadTriggeringCreatureUntilNextTurnEffect(), false, null, true),"),
    ]),
}

TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

TEST_EDITS = {
    "WildbornPreserverTest": [
        ("""    @Test
    @DisplayName("Wildborn Preserver does not trigger for its own entry")
""",
         """    @Test
    @DisplayName("The reflexive ability has no target: a Wildborn Preserver with shroud gets its counters")
    void reflexiveAbilityPutsCountersOnAPreserverWithShroud() {
        Permanent preserver = harness.addToBattlefieldAndReturn(player1, new WildbornPreserver());
        preserver.getGrantedKeywords().add(Keyword.SHROUD);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromHand(player1, new MaraleafRider(), "{1}{G}");
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wildborn Preserver does not trigger for its own entry")
"""),
        ("import com.github.laxika.magicalvibes.model.CounterType;\n",
         "import com.github.laxika.magicalvibes.model.CounterType;\n"
         "import com.github.laxika.magicalvibes.model.Keyword;\n"),
    ],
}


def apply(path, name, edits):
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old in text:
            text = text.replace(old, new, 1)
        else:
            sys.exit(f"{name}: edit does not apply:\n{old}")
    if text != original:
        path.write_text(text)
        print(f"converted {path.relative_to(ROOT)}")
        return 1
    return 0


def main():
    letters = {arg.lower() for arg in sys.argv[1:]}
    changed = 0
    for name, (module, edits) in sorted(EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(module=module, letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in edits:
            if old in text:
                text = text.replace(old, new, 1)
            elif new not in text:
                sys.exit(f"{name}: edit does not apply:\n{old}")
        if text != original:
            path.write_text(text)
            changed += 1
            print(f"converted {path.relative_to(ROOT)}")
    for name, edits in sorted(TEST_EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        changed += apply(ROOT / TESTS.format(letter=letter, name=name), name, edits)
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
