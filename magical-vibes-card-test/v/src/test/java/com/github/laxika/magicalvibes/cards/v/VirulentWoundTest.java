package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.m.MelirasKeepers;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({VirulentWound.class, GoForTheThroat.class, GrizzlyBears.class,
        LlanowarElves.class, Spellbook.class, Unsummon.class, MelirasKeepers.class})
class VirulentWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature")
    void putsCounterOnTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives controller poison counter when -1/-1 counter kills the creature")
    void givesPoisonWhenCounterKillsCreature() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Llanowar Elves (1/1) gets -1/-1 counter → 0/0 → dies from state-based actions
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        // Resolve the delayed triggered ability before checking the poison counter.
        if (!gd.stack.isEmpty()) harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives controller poison counter when creature dies later the same turn")
    void givesPoisonWhenCreatureDiesLaterThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        // Cast and resolve Virulent Wound — Grizzly Bears becomes 1/1, survives
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        // Cast and resolve Go for the Throat to kill the wounded creature
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Resolve the delayed triggered ability before checking the poison counter.
        if (!gd.stack.isEmpty()) harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("No poison counter when creature survives the turn")
    void noPoisonWhenCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Grizzly Bears (2/2) gets -1/-1 counter → effectively 1/1, survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        // No poison counter since creature didn't die
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Virulent Wound");
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No poison counter since spell fizzled
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Poison is received only when the delayed death trigger resolves")
    void poisonWaitsForTriggeredAbilityToResolve() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(gd.currentStep, () -> {
            harness.castAndResolveInstant(player1, 0,
                    harness.getPermanentId(player1, "Llanowar Elves"));

            harness.assertInGraveyard(player1, "Llanowar Elves");
            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());

            harness.passBothPriorities();
            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        });
    }

    @Test
    @DisplayName("Two Wounds create independently resolving poison triggers")
    void multipleWoundsCreateSeparateTriggers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound(), new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.withAutoStop(gd.currentStep, () -> {
            harness.castAndResolveInstant(player1, 0, targetId);
            harness.castAndResolveInstant(player1, 0, targetId);

            harness.assertInGraveyard(player2, "Grizzly Bears");
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.stack).hasSize(2);

            harness.passBothPriorities();
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("A bounced and recast creature is not tracked by the old Wound")
    void bouncedCreatureIsANewObject() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound(), new Unsummon(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castAndResolveInstant(player1, 0, originalId);
            harness.castAndResolveInstant(player1, 0, originalId);
            harness.assertInHand(player1, "Grizzly Bears");
            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();

            harness.castCreature(player1, 1);
            harness.passBothPriorities();
            Permanent returned = findPermanent(player1, "Grizzly Bears");
            assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
            harness.castAndResolveInstant(player1, 0, returned.getId());

            harness.assertInGraveyard(player1, "Grizzly Bears");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        });
    }

    @Test
    @DisplayName("Preventing the counter still registers the delayed poison trigger")
    void counterPreventionDoesNotPreventDelayedTrigger() {
        Permanent keepers = harness.addToBattlefieldAndReturn(player2, new MelirasKeepers());
        harness.setHand(player1, List.of(new VirulentWound(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(gd.currentStep, () -> {
            harness.castAndResolveInstant(player1, 0, keepers.getId());
            assertThat(keepers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

            harness.castAndResolveInstant(player1, 0, keepers.getId());
            harness.assertInGraveyard(player2, "Melira's Keepers");
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The death trigger expires after the turn but the counter remains")
    void delayedTriggerExpiresAfterTheTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentWound(), new GoForTheThroat()));
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveInstant(player1, 0, bears.getId()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castAndResolveInstant(player1, 0, bears.getId());
            harness.assertInGraveyard(player2, "Grizzly Bears");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        });
    }
}
