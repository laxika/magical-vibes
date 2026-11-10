#!/usr/bin/env python3
"""Step 15, runs 17z13c and 17z13d: the card tests of the converted cards.

- Hullbreaker Horror and Relic Bind: the mode choices name the card's own mode labels (the engine-made
  `ChoiceContext.HullbreakerHorrorModeChoice` / `RelicBindModeChoice` constants are gone; "do nothing" is the optional
  modal's `ChooseOneEffect.NO_MODE_LABEL`). New Hullbreaker Horror cases: the permanent mode's target is chosen among the
  legal targets (an opponent's hexproof permanent isn't one, a rules fix) and the spell mode needs a spell you don't
  control.
- Fangkeeper's Familiar (the trigger-time modal's spell-target modes): entering without being cast, the counter mode
  targets a creature spell as the ability goes on the stack, and isn't offered without one (rules fixes).
- Storm Cauldron: a land sacrificed as it is tapped for mana still triggers the return, which returns nothing (a rules
  fix).
- Corrosive Ooze: the Equipment of a creature that leaves while the block trigger is on the stack are still destroyed
  at end of combat.
- Reality Smasher: a granted ward keyword doesn't make its spell trigger see an opponent's ability (a rules fix).

Usage: step15-run17z13c-tests.py [REPO_ROOT] (the HEAD worktree for the rules-fix check). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

HULL_SPELL = "\"Return target spell you don't control to its owner's hand\""
HULL_PERMANENT = "\"Return target nonland permanent to its owner's hand\""
RELIC_DAMAGE = "\"This Aura deals 1 damage to target player or planeswalker\""
RELIC_LIFE = "\"Target player gains 1 life\""


def path_of(letter, name):
    return TESTS / letter / PKG / letter / f"{name}.java"


def edit(letter, name, pairs, done_marker, replace_all=()):
    path = path_of(letter, name)
    src = path.read_text()
    if done_marker in src:
        print("already converted", name)
        return
    for old, new in replace_all:
        if old not in src:
            sys.exit(f"{name}: expected\n{old}")
        src = src.replace(old, new)
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("converted", name)


def append_tests(src, methods):
    end = src.rstrip().rindex("}")
    return src[:end].rstrip() + "\n\n" + methods.rstrip() + "\n}\n"


def edit_append(letter, name, pairs, methods, done_marker):
    path = path_of(letter, name)
    src = path.read_text()
    if done_marker in src:
        print("already converted", name)
        return
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(append_tests(src, methods))
    print("converted", name)


HULLBREAKER_METHODS = f"""    @Test
    @DisplayName("Permanent mode can't target an opponent's hexproof permanent")
    void permanentModeCannotTargetOpponentsHexproofPermanent() {{
        harness.addToBattlefield(player1, new HullbreakerHorror());
        Permanent bogle = harness.addToBattlefieldAndReturn(player2, new SlipperyBogle());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0);
        harness.handleListChoice(player1, {HULL_PERMANENT});

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId()).doesNotContain(bogle.getId());
    }}

    @Test
    @DisplayName("The spell mode is offered only while a spell you don't control is on the stack")
    void spellModeNeedsASpellYouDontControl() {{
        harness.addToBattlefield(player1, new HullbreakerHorror());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0);

        PendingInteraction.ColorChoice modeChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(modeChoice).isNotNull();
        assertThat(modeChoice.options()).contains({HULL_PERMANENT}, ChooseOneEffect.NO_MODE_LABEL)
                .doesNotContain({HULL_SPELL});
    }}
"""


def hullbreaker():
    path = path_of("h", "HullbreakerHorrorTest")
    src = path.read_text()
    if "permanentModeCannotTargetOpponentsHexproofPermanent" in src:
        print("already converted HullbreakerHorrorTest")
        return
    # The permanent mode is chosen in several cases.
    path.write_text(src.replace(
            "harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.PERMANENT);",
            f"harness.handleListChoice(player1, {HULL_PERMANENT});"))
    edit_append("h", "HullbreakerHorrorTest", [
        ("import com.github.laxika.magicalvibes.cards.o.Opt;\n",
         "import com.github.laxika.magicalvibes.cards.o.Opt;\n"
         "import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;\n"),
        ("import com.github.laxika.magicalvibes.model.ChoiceContext;\n", ""),
        ("import com.github.laxika.magicalvibes.model.ManaColor;\n",
         "import com.github.laxika.magicalvibes.model.ManaColor;\n"
         "import com.github.laxika.magicalvibes.model.PendingInteraction;\n"),
        ("import com.github.laxika.magicalvibes.model.Permanent;\n",
         "import com.github.laxika.magicalvibes.model.Permanent;\n"
         "import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;\n"),
        ("@CardUsed({HullbreakerHorror.class, Cancel.class, GrizzlyBears.class, Opt.class})",
         "@CardUsed({HullbreakerHorror.class, Cancel.class, GrizzlyBears.class, Opt.class, SlipperyBogle.class})"),
        ("harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.SPELL);",
         f"harness.handleListChoice(player1, {HULL_SPELL});"),
        ("harness.handleListChoice(player1, ChoiceContext.HullbreakerHorrorModeChoice.NONE);",
         "harness.handleListChoice(player1, ChooseOneEffect.NO_MODE_LABEL);"),
    ], HULLBREAKER_METHODS, "permanentModeCannotTargetOpponentsHexproofPermanent")


def relic_bind():
    edit("r", "RelicBindTest", [
        ("import com.github.laxika.magicalvibes.model.ChoiceContext;\n", ""),
    ], RELIC_DAMAGE, replace_all=[
        ("ChoiceContext.RelicBindModeChoice.DAMAGE", RELIC_DAMAGE),
        ("ChoiceContext.RelicBindModeChoice.LIFE", RELIC_LIFE),
    ])


FANGKEEPER_METHODS = """    @Test
    @DisplayName("Entering without being cast, the counter mode targets a creature spell as the ability goes on the stack")
    void counterModeTargetsACreatureSpellWhenEnteringWithoutBeingCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        FaerieInvaders creatureSpell = new FaerieInvaders();
        harness.castFromHand(player2, creatureSpell, "{4}{U}");

        enterAndChooseMode("Counter target creature spell");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(creatureSpell.getId());
        harness.handlePermanentChosen(player1, creatureSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Faerie Invaders");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Faerie Invaders"));
    }

    @Test
    @DisplayName("Entering without being cast, the counter mode isn't offered without a creature spell to target")
    void counterModeIsNotOfferedWithoutACreatureSpell() {
        harness.enterBattlefieldAndReturn(player1, new FangkeepersFamiliar());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));

        PendingInteraction.ColorChoice modeChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(modeChoice).isNotNull();
        assertThat(modeChoice.options()).contains("You gain 3 life and surveil 3")
                .doesNotContain("Counter target creature spell");
    }
"""


def fangkeeper():
    edit_append("f", "FangkeepersFamiliarTest", [], FANGKEEPER_METHODS,
                "counterModeTargetsACreatureSpellWhenEnteringWithoutBeingCast")


STORM_CAULDRON_METHODS = """    @Test
    @DisplayName("A land sacrificed as it is tapped for mana still triggers the return, which returns nothing")
    void landSacrificedForManaTriggersButReturnsNothing() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new CrystalVein());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Cauldron");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Crystal Vein");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
"""


def storm_cauldron():
    edit_append("s", "StormCauldronTest", [
        ("import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;\n",
         "import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;\n"
         "import com.github.laxika.magicalvibes.cards.c.CrystalVein;\n"),
        ("@CardUsed({AshayaSoulOfTheWild.class, Forest.class,",
         "@CardUsed({AshayaSoulOfTheWild.class, CrystalVein.class, Forest.class,"),
    ], STORM_CAULDRON_METHODS, "landSacrificedForManaTriggersButReturnsNothing")


CORROSIVE_OOZE_METHODS = """    @Test
    void equipmentOfACreatureThatLeavesBeforeTheBlockTriggerResolvesIsStillDestroyed() {
        addReadyOoze(player2);
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(attacker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.sacrifice(attacker);
        harness.assertOnBattlefield(player1, "Short Sword");

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertNotOnBattlefield(player1, "Short Sword");
        harness.assertInGraveyard(player1, "Short Sword");
    }
"""


def corrosive_ooze():
    edit_append("c", "CorrosiveOozeTest", [], CORROSIVE_OOZE_METHODS,
                "equipmentOfACreatureThatLeavesBeforeTheBlockTriggerResolvesIsStillDestroyed")


REALITY_SMASHER_METHODS = """    @Test
    void grantedWardDoesNotMakeItsSpellTriggerSeeAnOpponentsActivatedAbility() {
        Permanent smasher = addReadyRealitySmasher();
        smasher.getGrantedKeywords().add(Keyword.WARD);
        Permanent drone = addCreatureReady(player2, new BlindingDrone());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, smasher.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(smasher.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
"""


def reality_smasher():
    edit_append("r", "RealitySmasherTest", [
        ("import com.github.laxika.magicalvibes.model.ManaColor;\n",
         "import com.github.laxika.magicalvibes.model.Keyword;\n"
         "import com.github.laxika.magicalvibes.model.ManaColor;\n"),
    ], REALITY_SMASHER_METHODS, "grantedWardDoesNotMakeItsSpellTriggerSeeAnOpponentsActivatedAbility")


if __name__ == "__main__":
    hullbreaker()
    relic_bind()
    fangkeeper()
    storm_cauldron()
    corrosive_ooze()
    reality_smasher()
