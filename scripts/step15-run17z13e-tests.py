#!/usr/bin/env python3
"""Step 15, run 17z13e: the collector service tests follow the life defaults.

`MiscTriggerCollectorServiceTest` scanned `LifeGainTriggerCollectorService` into its registry in some cases only (the
life handlers were split over both services); every life watch's handlers are that service's now, so the set-up scans
it once (a second scan would register its keys twice, which the registry rejects). The two Ob Nixilis cases asserted
the "exactly 1 life" gate of the deleted `SequenceEffect` handler: the gate is the card's event condition, judged by
`TriggerCollectionService` as the ability triggers (`ObNixilisCaptiveKingpinTest`), so one case asserts the default
records the life lost, the losing player and the source snapshot, and the other goes. Idempotent.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
MISC = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger/"
               "MiscTriggerCollectorServiceTest.java")

text = MISC.read_text()
text = text.replace("        TriggerCollectorRegistry.scanBean(new LifeGainTriggerCollectorService(gameLogService), registry);\n", "")
old_setup = "        registry = new TriggerCollectorRegistry();\n        TriggerCollectorRegistry.scanBean(sut, registry);\n"
new_setup = (old_setup
             + "        TriggerCollectorRegistry.scanBean(new LifeGainTriggerCollectorService(gameLogService), registry);\n")
if new_setup not in text:
    assert old_setup in text
    text = text.replace(old_setup, new_setup, 1)

once = re.compile(r'    @Test\n    @DisplayName\("exactly-one life-loss sequence trigger queues once"\)\n.*?\n    }\n', re.S)
text = once.sub('''    @Test
    @DisplayName("opponent life-loss default records the life lost, the losing player and the source, whatever the effects")
    void opponentLifeLossDefaultRecordsTheEvent() {
        Permanent perm = createPermanent("Ob Nixilis, Captive Kingpin");
        var effect = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ExileTopCardsMayPlayUntilNextEndStepEffect(1));

        assertThat(registry.dispatch(
                match(perm, player1Id, effect),
                LifeWatch.ON_OPPONENT_LOSES_LIFE,
                effect,
                new TriggerContext.LifeLoss(player2Id, 1))).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(1);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        });
    }
''', text, count=1)
larger = re.compile(r'    @Test\n    @DisplayName\("exactly-one life-loss sequence trigger ignores larger losses"\)\n.*?\n    }\n\n', re.S)
text = larger.sub('', text, count=1)
# The life default records the life amount whatever the effect's amount is, so the stubs of the deleted handlers'
# `referencesEventValue` check are unnecessary (Mockito's strict stubs reject them).
for stub in ("            when(amountEvaluationService.referencesEventValue(new EventValue())).thenReturn(true);\n\n",
             "            when(amountEvaluationService.referencesEventValue(effect.amount())).thenReturn(true);\n\n"):
    text = text.replace(stub, "")
# The enchanted-tap default records the tapped permanent's controller as the non-targeting "that player".
text = text.replace("""            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isNull();
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(tappedPerm.getId());""",
"""            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(tappedPerm.getId());""")
# Corrupted Roots says what it reads: the tapped land the entry records (TRIGGERING_PERMANENT_CONTROLLER).
text = text.replace('@DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TARGET_PERMANENT_CONTROLLER)")',
                    '@DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TRIGGERING_PERMANENT_CONTROLLER)")')
text = text.replace('@DisplayName("bakes the tapped land into targetId so its controller loses life at resolution")',
                    '@DisplayName("records the tapped land and its controller, which the loss reads as it resolves")')
text = text.replace("""            // targetId is the tapped land; TARGET_PERMANENT_CONTROLLER re-derives its controller.
            assertThat(stackEntry.getTargetId()).isEqualTo(tappedPerm.getId());""",
"""            assertThat(stackEntry.getTriggeringPermanentId()).isEqualTo(tappedPerm.getId());
            assertThat(stackEntry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);""")
text = text.replace("new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)",
                    "new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)")
MISC.write_text(text)

# Card cases (run per letter module: c, r).
CARD_TEST = ROOT / "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"


def add_case(letter, name, imports, used, case):
    path = pathlib.Path(str(CARD_TEST).format(letter, name))
    text = path.read_text()
    if case.split("void ")[1].split("(")[0] in text:
        return
    for line in imports:
        if line not in text:
            text = text.replace("import com.github.laxika.magicalvibes.model.", line + "import com.github.laxika.magicalvibes.model.", 1)
    for old, new in used:
        text = text.replace(old, new, 1)
    end = text.rstrip().rindex("}")
    text = text[:end].rstrip() + "\n\n" + case + "}\n"
    path.write_text(text)
    print("added a case to", name)


import sys as _sys
_letters = set(_sys.argv[1:])
if not _letters or "c" in _letters:
    add_case("c", "ChronicFloodingTest",
             ["import com.github.laxika.magicalvibes.cards.d.DryadArbor;\n",
              "import com.github.laxika.magicalvibes.cards.r.RayOfCommand;\n"],
             [("LeylineOfSanctity.class})", "LeylineOfSanctity.class, DryadArbor.class, RayOfCommand.class})")],
             '''    @Test
    @DisplayName("Its controller as the ability resolves mills, after the land changed control (CR 608.2h)")
    void landsControllerAsTheAbilityResolvesMills() {
        harness.forceActivePlayer(player1);
        Permanent arbor = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        arbor.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ChronicFlooding());
        aura.setAttachedTo(arbor.getId());
        harness.setLibrary(player1, library(6));
        harness.setLibrary(player2, library(6));
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.tapPermanent(player2, 0);
        harness.castAndResolveInstant(player1, 0, arbor.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arbor);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }
''')
if not _letters or "r" in _letters:
    add_case("r", "RoamingThroneTest",
             [],
             [("MarketGnome.class})", "MarketGnome.class, RatchetFieldMedic.class})")],
             '''    @Test
    @DisplayName("Roaming Throne doubles a \\"whenever you gain life, you may\\" ability of a creature of the chosen type (CR 603.2d)")
    void doublesChosenTypeCreatureLifeGainMayTrigger() {
        harness.forceActivePlayer(player1);
        addThrone(CardSubtype.ROBOT);
        harness.addToBattlefieldAndReturn(player1, new RatchetFieldMedic());

        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkLifeGainTriggers(gd, player1.getId(), 1));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Ratchet, Field Medic"))
                .hasSize(2);
    }
''')

# Mindcrank's trigger log came from the deleted mill handler (" triggers — X mills N cards."); the life default logs
# the trigger as every collector does ("Mindcrank's ability triggers."), the mill is logged as it resolves.
if not _letters or "m" in _letters:
    path = pathlib.Path(str(CARD_TEST).format("m", "MindcrankTest"))
    text = path.read_text()
    text = text.replace('''                log.contains("Mindcrank") && log.contains("triggers") && log.contains("mills"));''',
                        '''                log.contains("Mindcrank") && log.contains("triggers"));''')
    path.write_text(text)
