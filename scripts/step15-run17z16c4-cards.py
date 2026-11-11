#!/usr/bin/env python3
"""Step 15, runs 17z16c4 and 17z16c3b: the cards say what a handler inferred from an effect's class.

Oracle text checked with the Scryfall MCP for every card below.

"Create a token. When you do, [effect about that token]" (`CreateTokenThenEffect.aboutThatToken`): one reflexive
ability for each token created, with that token as the permanent the ability's effect acts for. The handler picked
these by the class of the follow-up effect.

- Ratonhnhaké:ton (ACR 62): "... create a 1/1 black Assassin creature token with menace. When you do, return target
  Equipment card from your graveyard to the battlefield, then attach it to that token."
- Dáin Ironfoot (HOB 91): "When Dáin enters, create a colorless Equipment artifact token named Axe ... When you do,
  attach it to target creature you control."
- Forum Filibuster (SOC 14): "... create a 2/1 white and black Inkling creature token with flying. When you do,
  return up to one target Aura or Equipment card from your graveyard to the battlefield attached to that token."

"Up to one target" is the effect's own (`EnteringCreatureFightsTargetCreatureEffect(predicate, true)`): the handler
of "create a token attached to target creature. When you do, ..." gave every follow-up an optional target.

- Curse of the Werefox (WOE 167): "Create a Monster Role token attached to target creature you control. When you
  do, that creature fights up to one target creature you don't control."

"Choose up to that many —" of a modal reflexive ability (`ChooseOneAtTriggerTimeEffect(modes, new XValue())`): the
number paid is the reflexive ability's X, and the most modes that may be chosen is read from it as the ability is
put on the stack (CR 700.2b). The handler rebuilt the card's `ChooseOneEffect`, by its class, with the number paid as
its maximum.

- Hawkeye, Master Marksman (MSH 130): "... you may pay {1} up to three times. When you do, choose up to that many —"
- Tranquil Frillback (MAT 24): "... you may pay {G} up to three times. When you pay this cost one or more times,
  choose up to that many —"

One ability, one stack entry: the choice between "that player or a planeswalker that player controls" is made as
the ability resolves and is not a target (the 2020-11-10 ruling: "After that opponent is chosen, you choose whether
damage will be dealt to that player or to a planeswalker they control ... No player may take any actions between
the damage recipient being chosen and the damage being dealt."). `DealDamageToRandomOpponentOrPlaneswalkerEffect`
already does that; the card's own `DealDamageToRandomOpponentOrTheirPlaneswalkerEffect` put a second, targeting
entry on the stack and is deleted.

- Vial Smasher the Fierce (SLD 1202): "Whenever you cast your first spell each turn, choose an opponent at random.
  Vial Smasher deals damage equal to that spell's mana value to that player or a planeswalker that player controls."

A mana ability's "when you do" (`QueueReflexiveAbilityEffect` after the mana): a reflexive ability that can't add
mana is not a mana ability (CR 605.5a), so it waits for the stack. The mana ability resolution picked these by the
class of the rider (a controller mill, damage to each opponent).

- Rubble Rouser (SOS 128): "{T}, Exile a card from your graveyard: Add {R}. When you do, this creature deals 1
  damage to each opponent."
- Shaun & Rebecca, Agents (ACR 64): "{T}: Add {C}. When you do, mill two cards."

Exert as the creature attacks: "You may exert this creature as it attacks. When you do, [effect]." The card writes
the exertion first and then its "when you do" as one reflexive ability (`QueueReflexiveAbilityEffect`), as Ahn-Crop
Champion, Ahn-Crop Crasher, Devoted Crop-Mate and Glorybringer already did; the accepted-exert handler split the
steps by class into the exertion, a stack entry of the plain steps and one reflexive ability per
`QueueReflexiveAbilityEffect`.

- Bitterblade Warrior (AKH 157): "... When you do, it gets +1/+0 and gains deathtouch until end of turn."
- Champion of Rhonas (AKH 159): "... When you do, you may put a creature card from your hand onto the battlefield."
- Clockwork Droid (WHO 172): "... When you do, it can't be blocked this turn and you scry 1."
- Combat Celebrant (AKH 125): "If this creature hasn't been exerted this turn, you may exert it as it attacks. When
  you do, untap all other creatures you control and after this phase, there is an additional combat phase."
- Emberhorn Minotaur (AKH 130): "... When you do, it gets +1/+1 and gains menace until end of turn."
- Glory-Bound Initiate (AKH 16): "... When you do, it gets +1/+3 and gains lifelink until end of turn."
- Gust Walker (AKH 17): "... When you do, it gets +1/+1 and gains flying until end of turn."
- Hooded Brawler (AKH 173): "... When you do, it gets +2/+2 until end of turn."
- Khenra Scrapper (HOU 100): "... When you do, it gets +2/+0 until end of turn."
- Nef-Crop Entangler (AKH 144): "... When you do, it gets +1/+2 until end of turn."
- Oketra's Avenger (HOU 17): "... When you do, prevent all combat damage that would be dealt to it this turn."
- Rhet-Crop Spearmaster (AKH 26): "... When you do, it gets +1/+0 and gains first strike until end of turn."
- Rhonas's Stalwart (HOU 133): "... When you do, it gets +1/+1 until end of turn and can't be blocked by creatures
  with power 2 or less this turn."
- Tah-Crop Elite (AKH 31): "... When you do, creatures you control get +1/+1 until end of turn."
- Themberchaud (SLD 728): "You may exert Themberchaud as he attacks. When you do, he gains flying until end of
  turn."
- Watchful Naga (AKH 193): "... When you do, draw a card."

Card tests: one new case each for Dáin Ironfoot, Jarsyl, Dark Age Scion, Spirit of Resilience and Vial Smasher the
Fierce (the rules fixes; each fails at `a3ce058a4e`). `AethermagesTouchTest` "Returns the creature at the end step of
its current controller" expected the opponent's Cytoplast Manipulator (DIS 23: "Graft 2 (... Whenever another creature
enters, you may move a +1/+1 counter from this creature onto it.)") to ask its "you may" before its graft
ability was on the stack; the ability goes on the stack and asks as it resolves (CR 603.5, CR 702.58a), so the test
passes priority first.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c4-cards.py [LETTER...]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
EFFECT = "import com.github.laxika.magicalvibes.model.effect."
QUEUE_IMPORT = EFFECT + "QueueReflexiveAbilityEffect;\n"

EXERT = "new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)"
EXERT_CARDS = [
    "BitterbladeWarrior", "ChampionOfRhonas", "ClockworkDroid", "CombatCelebrant", "EmberhornMinotaur",
    "GloryBoundInitiate", "GustWalker", "HoodedBrawler", "KhenraScrapper", "NefCropEntangler", "OketrasAvenger",
    "RhetCropSpearmaster", "RhonassStalwart", "TahCropElite", "Themberchaud", "WatchfulNaga",
]

EDITS = {
    "RatonhnhakTon": ("r", [
        ("new CreateTokenThenEffect(assassin,", "CreateTokenThenEffect.aboutThatToken(assassin,"),
    ]),
    "CurseOfTheWerefox": ("c", [
        ("                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));",
         "                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),\n"
         "                                true)));"),
    ]),
    "DainIronfoot": ("d", [
        ("new CreateTokenThenEffect(axe, new AttachCreatedEquipmentToTargetCreatureEffect())",
         "CreateTokenThenEffect.aboutThatToken(axe, new AttachCreatedEquipmentToTargetCreatureEffect())"),
    ]),
    "ForumFilibuster": ("f", [
        ("addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenThenEffect(",
         "addTrigger(Triggers.upkeep(ActivePlayer.YOU), CreateTokenThenEffect.aboutThatToken("),
    ]),
    "RubbleRouser": ("r", [
        ("                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)\n",
         "                        new QueueReflexiveAbilityEffect(\n"
         "                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))\n"),
        (QUEUE_IMPORT, QUEUE_IMPORT),
    ]),
    "HawkeyeMasterMarksman": ("h", [
        ("new PayManaUpToNTimesEffect(\"{1}\", 3, arrowModes)",
         "new PayManaUpToNTimesEffect(\"{1}\", 3,\n"
         "                new ChooseOneAtTriggerTimeEffect(arrowModes, new XValue()))"),
        (EFFECT + "ChooseOneAtTriggerTimeEffect;\n", EFFECT + "ChooseOneAtTriggerTimeEffect;\n"),
        ("import com.github.laxika.magicalvibes.model.amount.XValue;\n",
         "import com.github.laxika.magicalvibes.model.amount.XValue;\n"),
    ]),
    "TranquilFrillback": ("t", [
        ("new PayManaUpToNTimesEffect(\"{G}\", 3, modes)",
         "new PayManaUpToNTimesEffect(\"{G}\", 3, new ChooseOneAtTriggerTimeEffect(modes, new XValue()))"),
        (EFFECT + "ChooseOneAtTriggerTimeEffect;\n", EFFECT + "ChooseOneAtTriggerTimeEffect;\n"),
        ("import com.github.laxika.magicalvibes.model.amount.XValue;\n",
         "import com.github.laxika.magicalvibes.model.amount.XValue;\n"),
    ]),
    "VialSmasherTheFierce": ("v", [
        ("DealDamageToRandomOpponentOrTheirPlaneswalkerEffect", "DealDamageToRandomOpponentOrPlaneswalkerEffect"),
        ("DealDamageToRandomOpponentOrTheirPlaneswalkerEffect", "DealDamageToRandomOpponentOrPlaneswalkerEffect"),
    ]),
    "ShaunRebeccaAgents": ("s", [
        ("                        new MillEffect(2, MillRecipient.CONTROLLER)),\n",
         "                        new QueueReflexiveAbilityEffect(new MillEffect(2, MillRecipient.CONTROLLER))),\n"),
        (QUEUE_IMPORT, QUEUE_IMPORT),
    ]),
}

TEST_EDITS = {
    "AethermagesTouchTest": [
        ("""        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Cytoplast Manipulator");
""",
         """        // The opponent's graft ability goes on the stack and its "you may move a +1/+1 counter" is asked as it
        // resolves (CR 603.5).
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        Permanent entered = findPermanent(player1, "Cytoplast Manipulator");
"""),
    ],
    "DainIronfootTest": [
        ("""    @Test
    @DisplayName("ETB target selection excludes creatures an opponent controls")
""",
         """    @Test
    @DisplayName("With two Axes created, each has its own reflexive ability and its own target")
    void eachCreatedAxeIsAttachedByItsOwnAbility() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setHand(player1, List.of(new DainIronfoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dain = findPermanent(player1, "Dáin Ironfoot");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, dain.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Axe")).hasSize(2)
                .extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(bears.getId(), dain.getId());
    }

    @Test
    @DisplayName("ETB target selection excludes creatures an opponent controls")
"""),
        ("@CardUsed({DainIronfoot.class, GrizzlyBears.class, LeoninScimitar.class})",
         "@CardUsed({DainIronfoot.class, GrizzlyBears.class, LeoninScimitar.class, ParallelLives.class})"),
        ("import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;\n",
         "import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;\n"
         "import com.github.laxika.magicalvibes.cards.p.ParallelLives;\n"),
    ],
    "JarsylDarkAgeScionTest": [
        ("""    private void triggerBeginningOfCombat(Player activePlayer) {
""",
         """    @Test
    void intensifiesAsTheAbilityResolvesBeforeTheSpellCastResolves() {
        JarsylDarkAgeScion jarsylCard = new JarsylDarkAgeScion();
        Permanent jarsyl = harness.addToBattlefieldAndReturn(player1, jarsylCard);
        DarkRitual darkRitual = new DarkRitual();
        harness.setGraveyard(player1, List.of(darkRitual));

        triggerBeginningOfCombat(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        // "If you do, Jarsyl intensifies by 1" is part of the ability that let the spell be cast.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(darkRitual.getId());
        assertThat(gd.getCardIntensity(jarsyl.getCard().getId())).isEqualTo(2);
    }

    private void triggerBeginningOfCombat(Player activePlayer) {
"""),
    ],
    "SpiritOfResilienceTest": [
        ("""    @Test
    void laterBatchCannotCopyCardsFromAnEarlierBatch() {
""",
         """    @Test
    @DisplayName("Choosing one of several cards copies it as the ability resolves, with nothing put on the stack")
    void choosingAmongSeveralCardsCopiesAsTheAbilityResolves() {
        Permanent spirit = addSpirit();
        MindStone stone = new MindStone();
        PerpetualTimepiece timepiece = new PerpetualTimepiece();
        shuffleCardsFromGraveyard(stone, timepiece);

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(spirit.getCard().getName()).isEqualTo("Mind Stone");
    }

    @Test
    void laterBatchCannotCopyCardsFromAnEarlierBatch() {
"""),
    ],
    "VialSmasherTheFierceTest": [
        ("""    @Test
    @DisplayName("Can damage the opponent even when they control a planeswalker")
""",
         """    @Test
    @DisplayName("The damage is dealt as the ability resolves, once that player or their planeswalker is chosen")
    void theChoiceIsMadeAsTheAbilityResolves() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        castGrizzlyBears();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can damage the opponent even when they control a planeswalker")
"""),
    ],
}


def add_import(text, line):
    """Adds an import line in sorted position among the imports of the same package prefix."""
    if line in text:
        return text
    prefix = line[:line.rfind(".") + 1]
    imports = [m for m in re.finditer(r"^import [^\n]+;\n", text, re.M)]
    for match in imports:
        if match.group(0).startswith(prefix) and match.group(0) > line:
            return text[:match.start()] + line + text[match.start():]
    same = [m for m in imports if m.group(0).startswith(prefix)]
    last = (same or imports)[-1]
    return text[:last.end()] + line + text[last.end():]


def split_arguments(text):
    """Splits the text between a call's parentheses into its top-level arguments."""
    parts, depth, current, in_string = [], 0, "", False
    previous = ""
    for char in text:
        if char == '"' and previous != "\\":
            in_string = not in_string
        if not in_string:
            if char in "([{":
                depth += 1
            elif char in ")]}":
                depth -= 1
            elif char == "," and depth == 0:
                parts.append(current)
                current = ""
                previous = char
                continue
        current += char
        previous = char
    parts.append(current)
    return parts


def convert_exert(text, name):
    """Rewrites SequenceEffect.of(steps..., exert) as SequenceEffect.of(exert, reflexive(steps...))."""
    start = None
    for match in re.finditer(r"SequenceEffect\.of\(", text):
        depth, end = 1, match.end()
        while depth:
            depth += {"(": 1, ")": -1}.get(text[end], 0)
            end += 1
        arguments = [argument.strip() for argument in split_arguments(text[match.end():end - 1])]
        if EXERT in arguments:
            start = match.start()
            break
    if start is None:
        sys.exit(f"{name}: no exert sequence found")
    rest = [argument for argument in arguments if argument != EXERT]
    if any("QueueReflexiveAbilityEffect(" in argument.split("\n")[0] for argument in rest):
        return text
    line_start = text.rfind("\n", 0, start) + 1
    indent = re.match(r" *", text[line_start:]).group(0)
    step = indent + "        "
    if len(rest) == 1:
        reflexive = "new QueueReflexiveAbilityEffect(" + rest[0] + ")"
    else:
        inner = step + "        "
        reflexive = ("new QueueReflexiveAbilityEffect(SequenceEffect.of(\n"
                     + ",\n".join(inner + argument for argument in rest) + "))")
    replacement = ("SequenceEffect.of(\n" + step + EXERT + ",\n" + step + reflexive + "\n" + indent + ")")
    text = text[:start] + replacement + text[end:]
    return add_import(text, QUEUE_IMPORT)


def find(name, module=None):
    letter = name[0].lower()
    for candidate in ([module] if module else [letter, "shared"]):
        path = ROOT / CARDS.format(module=candidate, letter=letter, name=name)
        if path.exists():
            return path
    sys.exit(f"{name}: card class not found")


def apply(path, name, edits):
    text = path.read_text()
    original = text
    for old, new in edits:
        if old == new:
            text = add_import(text, new)
        elif new in text and (old not in text or old in new):
            continue
        elif old in text:
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
        if letters and name[0].lower() not in letters:
            continue
        changed += apply(find(name, module), name, edits)
    for name in EXERT_CARDS:
        if letters and name[0].lower() not in letters:
            continue
        path = find(name)
        text = path.read_text()
        converted = convert_exert(text, name)
        if converted != text:
            path.write_text(converted)
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
