#!/usr/bin/env python3
"""Step 15, run 17z13e5b: the collector tests follow the graveyard, sacrifice, milled and exiled defaults.

`DeathTriggerCollectorServiceTest`: the cases that called a deleted class-keyed handler call the watch's default and
assert its record (Fate Foretold's draw recipient as the non-targeting "that player", the artifact's controller as the
triggering permanent's, a "you may" as a plain entry, Sacred Ground's land through the land-by-opponent default, Pia's
Revolution's target choice through `handleAllyPermanentGraveyardDefault`, which carries the artifact card); a targeted
artifact ability waits in a `DeathTriggerTarget` carrying the artifact (the shared departure record) instead of a
`SpellTargetTriggerAnyTarget`; the opponent-permanent case uses the shared `AnyPermanentGraveyard` context, and the
Fate Foretold entry has its Aura's permanent id.

Card tests (`--cards d m p`): the rules fixes. A card that left the graveyard and came back before the ability
resolved is a new object (CR 400.7): `DesertWarfareTest.triggerFromAnEarlierStayDoesNotReturnTheNewDesert` and
`PiasRevolutionTest.artifactThatLeftAndReturnedIsNotReturned`; an artifact creature dying makes "whenever an artifact is
put into a graveyard from the battlefield" trigger an additional time under Teysa Karlov:
`MagneticMineTest.teysaDoublesForAnArtifactCreatureDying`. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DEATH = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger/"
                "DeathTriggerCollectorServiceTest.java")

MISC = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger/"
               "MiscTriggerCollectorServiceTest.java")

DEATH_NEW = ("departureAbilityFromExileRecordsThePermanent", "    @Nested\n    @DisplayName(\"handleEquippedCreatureDeathDefault\")", """    @Test
    @DisplayName("A departure ability of a card in exile has no source permanent and records the permanent that left")
    void departureAbilityFromExileRecordsThePermanent() {
        Card exiledSource = createCreature("Albiorix, Goose Tyrant", 3, 3);
        var effect = new DrawCardEffect(1);
        Card token = createArtifact("Food");
        token.setToken(true);
        Permanent sacrificed = new Permanent(token);

        svc.queueDepartureAbility(new TriggerMatchContext(gd, null, PLAYER1_ID, effect, exiledSource), effect, token,
                PLAYER1_ID, sacrificed, PLAYER1_ID);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(exiledSource);
            assertThat(entry.getSourcePermanentId()).isNull();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(token.getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(sacrificed.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getTargetId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }

    @Test
    @DisplayName("A land put into a graveyard: the land as it last existed and \\"that land's controller\\"")
    void landDefaultRecordsTheLandAndItsController() {
        Permanent watcher = new Permanent(createArtifact("Dingus Egg"));
        var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);
        Permanent departed = new Permanent(land);
        gd.graveyardEntryVersions.put(land.getId(), 4L);

        assertThat(svc.handleLandGraveyardDefault(match(watcher, PLAYER1_ID, effect), effect,
                new TriggerContext.AnyPermanentGraveyard(land, PLAYER2_ID, PLAYER2_ID, departed, 0, 0))).isTrue();

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(departed.getId());
            assertThat(entry.getTriggeringCardId()).isEqualTo(land.getId());
            assertThat(entry.isTriggeringCardIncarnation(4L)).isTrue();
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        });
    }

""")

MISC_NEW = ("opponentMillRecordsCountAndPlayer", "    @Test\n    void enchantedPermanentTapModalWaitsForModeAndTargetBeforeGoingOnTheStack()", """    @Test
    @DisplayName("An opponent's mill: the count is the event value, the milling player the non-targeting \\"that player\\"")
    void opponentMillRecordsCountAndPlayer() {
        Permanent source = createPermanent("Lo and Li, Royal Advisors");
        var effect = new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.ADVISOR));

        boolean collected = registry.dispatch(match(source, player1Id, effect), ZoneChangeWatch.ON_OPPONENT_MILLS,
                effect, new TriggerContext.Mill(player2Id, 3));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        });
    }

    @Test
    @DisplayName("A nonblack card put into a graveyard: the card, its owner \\"that player\\", from a source with no permanent")
    void nonblackCardRecordsTheCardAndItsOwner() {
        Card plane = createCard("The Dark Barony");
        var effect = new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER);
        Card card = createCard("Grizzly Bears");
        gd.graveyardEntryVersions.put(card.getId(), 5L);

        boolean collected = registry.dispatch(new TriggerMatchContext(gd, null, player1Id, effect, plane),
                ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, effect,
                new TriggerContext.CardPutIntoGraveyard(card, player2Id));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(plane);
            assertThat(entry.getSourcePermanentId()).isNull();
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(card.getId());
            assertThat(entry.isTriggeringCardIncarnation(5L)).isTrue();
        });
    }

    @Test
    @DisplayName("A creature exiled from the battlefield: \\"that card\\" with its exile incarnation, the creature as it last existed")
    void exiledCreatureRecordsTheCardAndTheCreature() {
        Permanent source = createPermanent("Athreos, Shroud-Veiled");
        var effect = new com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect(
                com.github.laxika.magicalvibes.model.Zone.EXILE);
        Permanent exiled = createPermanent("Grizzly Bears");
        gd.exileEntryVersions.put(exiled.getCard().getId(), 4L);

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD, effect,
                new TriggerContext.CreatureExiledFromBattlefield(exiled, player2Id, 2));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getTriggeringCardId()).isEqualTo(exiled.getCard().getId());
            assertThat(entry.getTriggeringCardExileEntryVersion()).isEqualTo(4L);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(exiled.getId());
            assertThat(entry.getTriggeringPermanentSnapshot()).isNotNull();
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(entry.getEventValue()).isEqualTo(2);
        });
    }

""")

DEATH_EDITS = [
    ('''        var context = new TriggerContext.OpponentPermanentGraveyard(
                dyingCard, PLAYER2_ID, PLAYER2_ID, dyingPermanent);

        assertThat(svc.handleOpponentPermanentGraveyardDefault(
                match(watcher, PLAYER1_ID, effect), effect, context)).isTrue();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);''',
     '''        var context = new TriggerContext.AnyPermanentGraveyard(
                dyingCard, PLAYER2_ID, PLAYER2_ID, dyingPermanent, 0, 0);

        assertThat(svc.handleOpponentPermanentGraveyardDefault(
                match(watcher, PLAYER1_ID, effect), effect, context)).isTrue();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        assertThat(entry.getTriggeringCardId()).isEqualTo(dyingCard.getId());
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(dyingPermanent.getId());
        assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
        assertThat(entry.getTargetId()).isEqualTo(PLAYER2_ID);
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();'''),
    ('''    @DisplayName("handleEnchantedCreatureControllerDrawsCard")
    class EnchantedCreatureControllerDrawsCard {

        @Test
        @DisplayName("Bakes the dying creature's controller as the draw target")
        void bakesControllerId() {''',
     '''    @DisplayName("handleEnchantedPermanentDeathDefault, \\"its controller draws a card\\"")
    class EnchantedCreatureControllerDrawsCard {

        @Test
        @DisplayName("Records the dying creature's controller as the non-targeting \\"that player\\"")
        void recordsControllerAsThatPlayer() {'''),
    ('''            svc.handleEnchantedCreatureControllerDrawsCard(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);''',
     '''            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).isNonTargeting()).isTrue();
            assertThat(gd.stack.get(0).getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);'''),
    ('''        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Optional Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);''',
     '''        @DisplayName("A \\"you may\\" goes on the stack as written, asked as it resolves")
        void mayEffectGoesOnTheStackAsWritten() {
            Card watcher = createArtifact("Optional Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardDefault(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(may);
            assertThat(gd.pendingMayAbilities).isEmpty();'''),
    ('''        @DisplayName("DealDamage effect sets target to artifact controller")
        void dealDamageSetsTarget() {
            Card watcher = createArtifact("Damage Watcher");
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleArtifactGraveyardDamageController(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);''',
     '''        @DisplayName("\\"That artifact's controller\\" is the triggering permanent's controller")
        void recordsTheArtifactsController() {
            Card watcher = createArtifact("Damage Watcher");
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            Permanent perm = new Permanent(watcher);
            Card artifact = createArtifact("Ornithopter");
            Permanent departed = new Permanent(artifact);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID, artifact, 0, departed, false);

            svc.handleArtifactGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(departed.getId());
            assertThat(entry.getTriggeringCardId()).isEqualTo(artifact.getId());'''),
    ('''            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.playerTargetOnly()).isTrue();
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
            assertThat(pending.sourcePermanentSnapshot()).isNotNull();
            assertThat(pending.effects()).containsExactly(effect);''',
     '''            var pending = (PermanentChoiceContext.DeathTriggerTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
            assertThat(pending.creaturesOnly()).isFalse();
            assertThat(pending.sourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
            assertThat(pending.effects()).containsExactly(effect);'''),
    ('''            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.departedPermanent().getCounters()).containsExactlyInAnyOrderEntriesOf(''',
     '''            var pending = (PermanentChoiceContext.DeathTriggerTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.triggeringPermanent().getCounters()).containsExactlyInAnyOrderEntriesOf('''),
    ('''        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Opponent May Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);''',
     '''        @DisplayName("A \\"you may\\" goes on the stack as written, the artifact's controller as \\"that player\\"")
        void mayEffectGoesOnTheStackAsWritten() {
            Card watcher = createArtifact("Opponent May Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardDefault(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(may);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).isNonTargeting()).isTrue();'''),
    ('''            var ctx = new TriggerContext.LandPutIntoGraveyard(land, PLAYER1_ID, PLAYER2_ID);

            assertThat(svc.handleLandGraveyardReturn(match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();''',
     '''            var ctx = new TriggerContext.AnyPermanentGraveyard(land, PLAYER1_ID, PLAYER1_ID, new Permanent(land), 0, 0);

            assertThat(svc.handleLandPutIntoGraveyardByOpponentDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();'''),
    ('''            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID, artifact, 0,
                    new Permanent(artifact), false);

            assertThat(svc.handleArtifactGraveyardReturnUnlessDamage(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertThat(gd.pendingInteractions)
                    .filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .singleElement()
                    .satisfies(choice -> {
                        var targetChoice = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) choice;''',
     '''            var ctx = new TriggerContext.AnyPermanentGraveyard(artifact, PLAYER1_ID, PLAYER1_ID,
                    new Permanent(artifact), 0, 0);

            assertThat(svc.handleAllyPermanentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertThat(gd.pendingInteractions)
                    .filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance)
                    .singleElement()
                    .satisfies(choice -> {
                        var targetChoice = (PermanentChoiceContext.DeathTriggerTarget) choice;'''),
]


def edit(path, edits):
    text = path.read_text()
    for old, new in edits:
        if new in text:
            continue
        assert old in text, old[:120]
        text = text.replace(old, new)
    path.write_text(text)


CARD_TESTS = {
    "d": ("DesertWarfareTest", "triggerFromAnEarlierStayDoesNotReturnTheNewDesert", """
    @Test
    @DisplayName("A Desert that left the graveyard and returned before the ability resolves is not returned")
    void triggerFromAnEarlierStayDoesNotReturnTheNewDesert() {
        harness.addToBattlefield(player1, new DesertWarfare());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        HashepOasis desert = new HashepOasis();
        harness.setLibrary(player1, List.of(desert, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(millstone),
                0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getName()).isEqualTo("Desert Warfare"));

        harness.setGraveyard(player1, List.of());
        gd.markGraveyardExit(desert.getId());
        harness.setGraveyard(player1, List.of(desert));
        gd.markGraveyardEntry(desert);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hashep Oasis");
        harness.assertNotOnBattlefield(player1, "Hashep Oasis");
    }
""", []),
    "m": ("MagneticMineTest", "teysaDoublesForAnArtifactCreatureDying", """
    @Test
    @DisplayName("Teysa Karlov makes it trigger an additional time when an artifact creature dies")
    void teysaDoublesForAnArtifactCreatureDying() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.addToBattlefield(player2, new Memnite());
        harness.setLife(player2, 20);
        UUID memniteId = harness.getPermanentId(player2, "Memnite");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, memniteId);

        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Magnetic Mine")).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }
""", ["com.github.laxika.magicalvibes.cards.t.TeysaKarlov"]),
    "p": ("PiasRevolutionTest", "artifactThatLeftAndReturnedIsNotReturned", """
    @Test
    @DisplayName("An artifact card that left the graveyard and returned before the ability resolves isn't returned")
    void artifactThatLeftAndReturnedIsNotReturned() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player1, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        destroyArtifact(mindStoneId);
        harness.handlePermanentChosen(player1, player2.getId());
        Card mindStone = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Mind Stone"))
                .findFirst().orElseThrow();
        harness.setGraveyard(player1, List.of());
        gd.markGraveyardExit(mindStone.getId());
        harness.setGraveyard(player1, List.of(mindStone));
        gd.markGraveyardEntry(mindStone);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotInHand(player1, "Mind Stone");
    }
""", ["org.junit.jupiter.api.DisplayName"]),
}


def add_card_test(letter):
    name, method, body, imports = CARD_TESTS[letter]
    path = ROOT / f"magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    if f"void {method}()" not in text:
        anchor = "\n    private " if "\n    private " in text else None
        index = text.index(anchor) if anchor else text.rstrip().rindex("}")
        text = text[:index] + body.rstrip("\n") + "\n" + text[index:]
    for fqcn in imports:
        line = f"import {fqcn};\n"
        if line not in text:
            first = text.index("import org.junit")
            text = text[:first] + line + text[first:]
        simple = fqcn.rsplit(".", 1)[1]
        if fqcn.startswith("com.github.laxika.magicalvibes.cards.") and "@CardUsed({" in text \
                and simple + ".class" not in text[text.index("@CardUsed({"):text.index("})", text.index("@CardUsed({"))]:
            text = text.replace("@CardUsed({", "@CardUsed({" + simple + ".class, ", 1)
    path.write_text(text)


def insert_tests(path, new):
    method, anchor, body = new
    text = path.read_text()
    if f"void {method}()" not in text:
        assert anchor in text, anchor
        text = text.replace(anchor, body + anchor, 1)
    text = text.replace("    // ===== Helpers =====\n\n", "")
    path.write_text(text)


def main():
    edit(DEATH, DEATH_EDITS)
    insert_tests(DEATH, DEATH_NEW)
    insert_tests(MISC, MISC_NEW)
    if "--cards" in sys.argv:
        for letter in sys.argv[sys.argv.index("--cards") + 1:]:
            add_card_test(letter)


if __name__ == "__main__":
    main()
