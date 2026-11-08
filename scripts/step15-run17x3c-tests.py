#!/usr/bin/env python3
"""Step 15, run 17x3c: the graft and Agent's Toolkit card tests answer the "you may" as the ability resolves.

Graft's and Agent's Toolkit's "you may move a counter" abilities were asked before they went on the stack (a pending
may ability the enter collector queued); they now go on the stack and ask as they resolve (CR 603.5). A test that
answered the may and then passed priority to resolve the ability passes priority first and then answers; a test that
changed the game while the ability waited does so before passing priority. Two rules-fix tests are added: graft goes on
the stack and asks as it resolves (Simic Initiate, CR 603.5), and protection prevents the damage "it" is dealt by an
ability that reads the stack entry's triggering permanent (Circle of Flame, CR 702.16e). Agent's Toolkit's "the may is offered"
assertions become "the ability is on the stack". Llanowar Reborn's helper already resolves the ability, so only its
two "before resolution" tests change, to leave the ability on the stack while they change the game.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17x3c-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

GENERIC = ["AquastrandSpiderTest", "CytoplastManipulatorTest", "CytoplastRootKinTest", "CytospawnShamblerTest",
           "HeliumSquirterTest", "NovijenSagesTest", "PlaxcasterFroglingTest", "SimicBasiliskTest",
           "SimicInitiateTest", "SporebackTrollTest", "VigeanGraftmageTest", "VigeanHydroponTest",
           "AgentsToolkitTest"]

# The may answered, then (optionally) the game changed, then priority passed: change, pass, then answer.
ANSWER_THEN_PASS = re.compile(
    r"(        harness\.handleMayAbilityChosen\((\w+), (true|false)\);\n)"
    r"((?:(?!        harness\.passBothPriorities\(\);\n)(?!.*assertThat)(?!.*handleMayAbilityChosen)"
    r"(?!.*handleListChoice).*\n)*?)"
    r"(        harness\.passBothPriorities\(\);\n)")
# A decline with no pass after it: pass first so the ability resolves and asks.
DECLINE_ALONE = re.compile(r"(?<!harness\.passBothPriorities\(\);\n)"
                           r"(        harness\.handleMayAbilityChosen\((\w+), false\);\n\n)")

# An answer followed by resolveAllTriggers(): resolve the ability first so it asks.
ANSWER_THEN_RESOLVE_ALL = re.compile(r"(?<!harness\.passBothPriorities\(\);\n)"
                                     r"(        harness\.handleMayAbilityChosen\((\w+), (true|false)\);\n"
                                     r"        resolveAllTriggers\(\);\n)")

# Vigean Hydropon's "has left" test moved the permanent by hand, so no last-known information told graft's
# intervening "if" that it had a counter: it now leaves the battlefield through the engine (a sacrifice).
HYDROPON_EDITS = [
    ("        gd.playerBattlefields.get(player1.getId()).remove(hydropon);\n"
     "        gd.playerGraveyards.get(player1.getId()).add(hydropon.getCard());\n",
     "        harness.sacrifice(hydropon);\n"),
]

TOOLKIT_EDITS = [
    ("        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))\n"
     "                .isNotNull();\n",
     "        assertThat(gd.stack).hasSize(1);\n"),
    ("        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();\n",
     "        assertThat(gd.stack).hasSize(1);\n"),
]

LLANOWAR_EDITS = [
    ("        castCreature(player1);\n"
     "        harness.handleMayAbilityChosen(player1, true);\n"
     "\n"
     "        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);\n"
     "        harness.passBothPriorities();\n",
     "        castCreatureLeavingGraftOnTheStack(player1);\n"
     "\n"
     "        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);\n"
     "        harness.passBothPriorities();\n"
     "\n"
     "        // \"If this permanent has a +1/+1 counter on it\" is checked again as the ability resolves (CR 603.4).\n"
     "        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();\n"),
    ("        castCreature(player1);\n"
     "        harness.handleMayAbilityChosen(player1, true);\n"
     "\n"
     "        Permanent creature = findPermanent(player1, \"Grizzly Bears\");\n"
     "        gd.playerBattlefields.get(player1.getId()).remove(creature);\n"
     "        gd.playerHands.get(player1.getId()).add(creature.getCard());\n"
     "        harness.passBothPriorities();\n",
     "        castCreatureLeavingGraftOnTheStack(player1);\n"
     "\n"
     "        Permanent creature = findPermanent(player1, \"Grizzly Bears\");\n"
     "        gd.playerBattlefields.get(player1.getId()).remove(creature);\n"
     "        gd.playerHands.get(player1.getId()).add(creature.getCard());\n"
     "        harness.passBothPriorities();\n"
     "        harness.handleMayAbilityChosen(player1, true);\n"),
    ("    private void castCreature(com.github.laxika.magicalvibes.model.Player player) {\n"
     "        harness.forceActivePlayer(player);\n"
     "        harness.forceStep(TurnStep.PRECOMBAT_MAIN);\n"
     "        harness.castFromHand(player, new GrizzlyBears(), \"{1}{G}\");\n"
     "        harness.passBothPriorities();\n"
     "        harness.passBothPriorities();\n"
     "    }\n",
     "    private void castCreature(com.github.laxika.magicalvibes.model.Player player) {\n"
     "        castCreatureLeavingGraftOnTheStack(player);\n"
     "        harness.passBothPriorities();\n"
     "    }\n"
     "\n"
     "    private void castCreatureLeavingGraftOnTheStack(com.github.laxika.magicalvibes.model.Player player) {\n"
     "        harness.forceActivePlayer(player);\n"
     "        harness.forceStep(TurnStep.PRECOMBAT_MAIN);\n"
     "        harness.castFromHand(player, new GrizzlyBears(), \"{1}{G}\");\n"
     "        harness.passBothPriorities();\n"
     "    }\n"),
]

# New tests: rules fixes of this run, inserted before the class's first helper (or its closing brace).
NEW_TESTS = {
    "SimicInitiateTest": (
        "graftGoesOnTheStackAndAsksAsItResolves",
        "    @Test\n"
        "    @DisplayName(\"Graft goes on the stack and its controller chooses as it resolves (CR 603.5)\")\n"
        "    void graftGoesOnTheStackAndAsksAsItResolves() {\n"
        "        Permanent initiate = castInitiate();\n"
        "\n"
        "        harness.castFromHand(player1, new TransguildCourier(), \"{4}\");\n"
        "        harness.passBothPriorities();\n"
        "        Permanent courier = findPermanent(player1, \"Transguild Courier\");\n"
        "\n"
        "        assertThat(gd.stack).hasSize(1);\n"
        "        assertThat(gd.pendingMayAbilities).isEmpty();\n"
        "        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();\n"
        "\n"
        "        harness.passBothPriorities();\n"
        "        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();\n"
        "        harness.handleMayAbilityChosen(player1, true);\n"
        "\n"
        "        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(initiate);\n"
        "        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);\n"
        "    }\n\n",
        "    private Permanent castInitiate() {",
        ["import com.github.laxika.magicalvibes.model.PendingInteraction;"], None),
    "CircleOfFlameTest": (
        "protectionFromRedPreventsTheDamage",
        "    @Test\n"
        "    @DisplayName(\"Protection from red prevents the damage to the attacker (CR 702.16e)\")\n"
        "    void protectionFromRedPreventsTheDamage() {\n"
        "        Permanent attacker = setUpAttack(new BurrentonForgeTender());\n"
        "\n"
        "        declareAttackers(player2, List.of(0));\n"
        "        assertThat(gd.stack).hasSize(1);\n"
        "        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));\n"
        "\n"
        "        assertThat(attacker.getMarkedDamage()).isZero();\n"
        "    }\n\n",
        "    @Test\n    @DisplayName(\"Shroud does not prevent the non-targeting damage ability\")",
        ["import com.github.laxika.magicalvibes.cards.b.BurrentonForgeTender;"],
        ("@CardUsed({CircleOfFlame.class,", "@CardUsed({CircleOfFlame.class, BurrentonForgeTender.class,")),
}


def insert_new_test(name, text):
    method, body, anchor, imports, card_used = NEW_TESTS[name]
    if f"void {method}()" in text:
        return text
    if anchor not in text:
        raise SystemExit(f"{name}: anchor not found: {anchor}")
    text = text.replace(anchor, body + anchor, 1)
    lines = text.split("\n")
    for line in imports:
        if line in lines:
            continue
        own = [i for i, l in enumerate(lines) if l.startswith("import com.")]
        position = next((i for i in own if lines[i] > line), own[-1] + 1)
        lines.insert(position, line)
    text = "\n".join(lines)
    if card_used is not None:
        text = text.replace(card_used[0], card_used[1], 1)
    return text


def find_test(name, modules):
    for module in modules:
        base = ROOT / f"magical-vibes-card-test/{module}/src/test/java/com/github/laxika/magicalvibes/cards"
        for path in base.glob(f"*/{name}.java"):
            return path
    return None


def convert_generic(text):
    text = ANSWER_THEN_RESOLVE_ALL.sub(lambda m: "        harness.passBothPriorities();\n" + m.group(1), text)
    text = ANSWER_THEN_PASS.sub(lambda m: m.group(4) + m.group(5)
                                + f"        harness.handleMayAbilityChosen({m.group(2)}, {m.group(3)});\n", text)
    return DECLINE_ALONE.sub(lambda m: "        harness.passBothPriorities();\n" + m.group(1), text)


def apply_edits(name, text, edits):
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    return text


def convert(name, path):
    text = path.read_text()
    original = text
    if name == "LlanowarRebornTest":
        text = apply_edits(name, text, LLANOWAR_EDITS)
    elif name in GENERIC:
        if name == "VigeanHydroponTest":
            text = apply_edits(name, text, HYDROPON_EDITS)
        if name == "AgentsToolkitTest":
            for old, new in TOOLKIT_EDITS:
                text = text.replace(old, new)
        text = convert_generic(text)
    if name in NEW_TESTS:
        text = insert_new_test(name, text)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for name in GENERIC + ["LlanowarRebornTest", "CircleOfFlameTest"]:
        path = find_test(name, modules)
        if path is not None:
            convert(name, path)


if __name__ == "__main__":
    main()
