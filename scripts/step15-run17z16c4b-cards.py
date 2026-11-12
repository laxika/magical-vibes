#!/usr/bin/env python3
"""Step 15, run 17z16c4b: the exert cards write the exertion by name, and the card tests that waited for an accepted
option to resolve from the stack no longer do.

A triggered ability's "you may" goes on the stack with the ability and is asked as it resolves (CR 603.5). An option
a resolving spell or ability offers, or one chosen as an event happens, is carried out at once (CR 608.2d); it was
put on the stack as an ability of its own once accepted.

Cards (`EXERT`, 25 classes). Oracle text checked with the Scryfall MCP for Glory-Bound Initiate (AKH 16): "You may
exert this creature as it attacks. When you do, it gets +1/+3 and gains lifelink until end of turn. (An exerted
creature won't untap during your next untap step.)", with its 2017-04-18 ruling: "All cards in the Amonkhet set that
let you exert a creature let you do so as you declare it as an attacking creature ... Any abilities that trigger on
exerting an attacking creature will resolve before blockers are declared." The exertion each card wrote as
`new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)` is `SkipNextUntapEffect.exert()`, the same
record: the attack collector asks the lone "you may" whether it exerts as it attacks (`CardEffect.exertsAsItAttacks()`)
instead of testing its step's class and flag. The other 24 cards write the same first step in the "you may" of their
`Triggers.attacks()` ability; their texts were not fetched again (run 17z16c4 read the 16 it converted).

Card tests (`TEST_EDITS`). Each was read against the card's Oracle text (Scryfall MCP), which has the option
carried out as the spell or ability that offers it resolves:

- Heartwood Storyteller (FUT 127): "Whenever a player casts a noncreature spell, each of that player's opponents may
  draw a card."
- Arcum Dagsson (CSP 27): "{T}: Target artifact creature's controller sacrifices it. That player may search their
  library for a noncreature artifact card, put it onto the battlefield, then shuffle."
- The Day of the Doctor (WHO 121), chapter IV: "Choose up to three Doctors. You may exile all other creatures. If you
  do, this Saga deals 13 damage to you."
- Rona, Tolarian Obliterator (MOM 75, back face): "Whenever a source deals damage to Rona, that source's controller
  exiles a card from their hand at random. If it's a land card, you may put it onto the battlefield under your
  control. Otherwise, you may cast it without paying its mana cost."
- Kellan, the Kid (OTJ 213): "... you may cast a permanent spell with equal or lesser mana value from your hand
  without paying its mana cost. If you don't, you may put a land card from your hand onto the battlefield."
- Tibalt, Wicked Tormentor (YMID 43): "+1: Tibalt, Wicked Tormentor deals 4 damage to target creature or planeswalker
  unless its controller has Tibalt deal 4 damage to them. If they do, you may discard a card. If you do, draw a
  card."
- Liar's Pendulum (MRD 196): "{2}, {T}: Choose a card name. Target opponent guesses whether a card with that name is
  in your hand. You may reveal your hand. If you do and your opponent guessed wrong, draw a card."
- Curse of Echoes (DKA 34): "Whenever enchanted player casts an instant or sorcery spell, each other player may copy
  that spell and may choose new targets for the copy they control."
- Baral and Kari Zev (MOM 218): "... you may cast a spell with lesser mana value that shares a card type with it
  from your hand without paying its mana cost. If you don't, create First Mate Ragavan, a legendary 2/1 red Monkey
  Pirate creature token. It gains haste until end of turn."

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c4b-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

EXERT = [
    "AhnCropChampion", "AhnCropCrasher", "BattlefieldScavenger", "BitterbladeWarrior", "ChampionOfRhonas",
    "ClockworkDroid", "CombatCelebrant", "DevotedCropMate", "EmberhornMinotaur", "GloryBoundInitiate",
    "Glorybringer", "GustWalker", "HoodedBrawler", "KhenraScrapper", "NefCropEntangler", "OketrasAvenger",
    "ResoluteSurvivors", "RhetCropSpearmaster", "RhonassStalwart", "RohirrimChargers", "TahCropElite",
    "Themberchaud", "TrueheartTwins", "VizierOfTheTrue", "WatchfulNaga",
]
OLD_EXERTION = "new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)"
NEW_EXERTION = "SkipNextUntapEffect.exert()"
SCOPE_IMPORT = "import com.github.laxika.magicalvibes.model.effect.TapUntapScope;\n"

TEST_EDITS = {
    "ArcumDagssonTest": [
        ("""        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
""",
         """        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
"""),
        ("""        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);""",
         """        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);"""),
        ("""        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);""",
         """        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);"""),
        ("""        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);""",
         """        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);"""),
        ("""        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
""",
         """        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
"""),
    ],
    "TheDayOfTheDoctorTest": [
        ("""        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""                List.of(ownTenth.getId(), ownFourth.getId(), opposingTenth.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """                List.of(ownTenth.getId(), ownFourth.getId(), opposingTenth.getId()));
        harness.handleMayAbilityChosen(player1, true);
"""),
    ],
    "RonaHeraldOfInvasionTest": [
        ("""                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
"""),
    ],
    "TibaltWickedTormentorTest": [
        ("""        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);""",
         """        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);"""),
        ("""        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player1, true);
"""),
    ],
    "LiarsPendulumTest": [
        ("""        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "No");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "No");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "Yes");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        activateAndChooseName("Myr Retriever");
        harness.handleListChoice(player2, "Yes");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "Yes");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "Yes");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "No");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        activateAndChooseName("Bonesplitter");
        harness.handleListChoice(player2, "No");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
"""),
        ("""        harness.handleListChoice(player2, "Yes");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
""",
         """        harness.handleListChoice(player2, "Yes");
        harness.handleMayAbilityChosen(player1, true);
"""),
    ],
    "KellanTheKidTest": [
        ("""        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();""",
         """        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();"""),
    ],
    "HeartwoodStorytellerTest": [
        ("""        castNoncreatureSpell(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();
""",
         """        castNoncreatureSpell(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
"""),
        ("""        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The caster is not offered a draw")""",
         """        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("The caster is not offered a draw")"""),
        ("""import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;""",
         """import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;"""),
    ],
    "BaralAndKariZevTest": [
        ("""        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
""",
         """        harness.handleMayAbilityChosen(player1, false);
"""),
        ("""        harness.handleMayAbilityChosen(player1, false);

        Permanent ragavan = gd.playerBattlefields.get(player1.getId()).stream()""",
         """        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        Permanent ragavan = gd.playerBattlefields.get(player1.getId()).stream()"""),
        ("""import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;""",
         """import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;"""),
    ],
}


def letters():
    return {argument.lower() for argument in sys.argv[1:]} or None


def convert_exert(name, chosen):
    letter = name[0].lower()
    if chosen is not None and letter not in chosen:
        return False
    path = ROOT / CARDS.format(letter=letter, name=name)
    text = path.read_text()
    if OLD_EXERTION not in text:
        if NEW_EXERTION not in text:
            sys.exit(f"{name}: no exertion to convert")
        return False
    text = text.replace(OLD_EXERTION, NEW_EXERTION)
    if "TapUntapScope." not in text:
        text = text.replace(SCOPE_IMPORT, "")
    path.write_text(text)
    return True


def apply_edits(name, edits, chosen):
    letter = name[0].lower()
    if chosen is not None and letter not in chosen:
        return False
    path = ROOT / TESTS.format(letter=letter, name=name)
    text = path.read_text()
    changed = False
    for old, new in edits:
        if old in text:
            if text.count(old) != 1:
                sys.exit(f"{name}: edit is not unique: {old[:60]!r}")
            text = text.replace(old, new)
            changed = True
        elif new not in text:
            sys.exit(f"{name}: edit does not apply: {old[:60]!r}")
    if changed:
        path.write_text(text)
    return changed


def main():
    chosen = letters()
    cards = [name for name in EXERT if convert_exert(name, chosen)]
    tests = [name for name, edits in TEST_EDITS.items() if apply_edits(name, edits, chosen)]
    print(f"{len(cards)} card classes converted" + (": " + ", ".join(cards) if cards else ""))
    print(f"{len(tests)} card test classes changed" + (": " + ", ".join(tests) if tests else ""))


if __name__ == "__main__":
    main()
