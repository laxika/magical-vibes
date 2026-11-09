#!/usr/bin/env python3
"""Step 15, run 17z11f2: card tests (idempotent; optional repository-root argument, used for the HEAD worktree).

- Venser, the Sojourner's emblem tests get the emblem by the -8 ability instead of building one around the removed
  `ExileTargetOnControllerSpellCastEffect` marker.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"


def edit(relative, replacements):
    path = TESTS / relative
    text = path.read_text()
    for old, new in replacements:
        # Done when the old text is gone, or (a helper inserted before an anchor it keeps) the new text is there.
        if (old not in text or old in new) and new in text:
            continue
        if text.count(old) != 1:
            sys.exit(f"{relative}: anchor not found once: {old[:70]!r}")
        text = text.replace(old, new)
    path.write_text(text)


VENSER = "v/src/test/java/com/github/laxika/magicalvibes/cards/v/VenserTheSojournerTest.java"
edit(VENSER, [
    ("import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;\n", ""),
    ("""        addReadyVenser(player1);
        // Manually create the emblem (simulating ultimate already resolved)
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), new VenserTheSojourner());
        gd.emblems.add(emblem);
""", """        getEmblem(player1);
"""),
    ("""        addReadyVenser(player1);
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), new VenserTheSojourner());
        gd.emblems.add(emblem);
""", """        getEmblem(player1);
"""),
    ("""    private Permanent addReadyVenser(Player player) {""", """    private void getEmblem(Player player) {
        Permanent venser = addReadyVenser(player);
        venser.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);
    }

    private Permanent addReadyVenser(Player player) {"""),
])
JODAH = "j/src/test/java/com/github/laxika/magicalvibes/cards/j/JodahTheUnifierTest.java"
edit(JODAH, [
    ("import com.github.laxika.magicalvibes.cards.m.MoxAmber;\n",
     "import com.github.laxika.magicalvibes.cards.m.MoxAmber;\nimport com.github.laxika.magicalvibes.cards.r.RoamingThrone;\n"),
    ("import com.github.laxika.magicalvibes.model.Card;\n",
     "import com.github.laxika.magicalvibes.model.Card;\nimport com.github.laxika.magicalvibes.model.CardSubtype;\n"),
    ("IsamaruHoundOfKonda.class, LlanowarElves.class})",
     "IsamaruHoundOfKonda.class, LlanowarElves.class,\n        RoamingThrone.class})"),
    ("""    private void prepareCasterTurn() {""", """    @Test
    @DisplayName("Jodah's ability is its own, so Roaming Throne naming Human makes it trigger an additional time")
    void roamingThroneDoublesJodahsAbility() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());
        Permanent throne = new Permanent(new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.HUMAN);
        gd.playerBattlefields.get(player1.getId()).add(throne);
        harness.setLibrary(player1, List.of(new HillGiant(), new IsamaruHoundOfKonda(), new LlanowarElves()));

        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2)
                .allMatch(entry -> entry.getCard().getName().equals("Jodah, the Unifier"));
    }

    private void prepareCasterTurn() {"""),
])
CONDUIT = "c/src/test/java/com/github/laxika/magicalvibes/cards/c/ConduitOfRuinTest.java"
edit(CONDUIT, [
    ("import com.github.laxika.magicalvibes.model.PendingInteraction;\n",
     "import com.github.laxika.magicalvibes.model.PendingInteraction;\nimport com.github.laxika.magicalvibes.model.StackEntryType;\n"),
    ("""    private void castConduit(Card... library) {""", """    @Test
    @DisplayName("The cast trigger goes on the stack and its optional search is asked as it resolves")
    void castTriggerAsksItsMayAsItResolves() {
        harness.setHand(player1, List.of(new ConduitOfRuin()));
        harness.setLibrary(player1, List.of(new RuinProcessor()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castConduit(Card... library) {"""),
])
print("tests edited")
