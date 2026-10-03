package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkslickDrake.class, GrizzlyBears.class, WrathOfGod.class, SteelHellkite.class, Disperse.class})
class DarkslickDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Darkslick Drake dies blocking a bigger creature, draws a card")
    void diesInCombatAsBlockerDrawsCard() {
        Permanent drakePerm = addCreatureReady(player1, new DarkslickDrake());
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new SteelHellkite());
        attacker.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);

        // Darkslick Drake should be dead
        harness.assertNotOnBattlefield(player1, "Darkslick Drake");
        harness.assertInGraveyard(player1, "Darkslick Drake");

        // Triggered ability should be on the stack (mandatory, no may prompt)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Darkslick Drake dies as attacker blocked by bigger creature, draws a card")
    void diesInCombatAsAttackerDrawsCard() {
        Permanent drakePerm = addCreatureReady(player1, new DarkslickDrake());
        drakePerm.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SteelHellkite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player1);

        // Darkslick Drake should be dead
        harness.assertInGraveyard(player1, "Darkslick Drake");

        // Triggered ability on stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Darkslick Drake dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new DarkslickDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Cast Wrath of God
        harness.castSorcery(player1, 0);

        // Resolve Wrath of God — all creatures are destroyed
        harness.passBothPriorities();

        // Darkslick Drake should be dead
        harness.assertNotOnBattlefield(player1, "Darkslick Drake");
        harness.assertInGraveyard(player1, "Darkslick Drake");

        // Triggered ability on stack (mandatory)
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Hand should be empty (Wrath went to graveyard) + 1 drawn card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Darkslick Drake survives combat, no death trigger fires")
    void survivesNoCombatDeathTrigger() {
        Permanent drakePerm = addCreatureReady(player1, new DarkslickDrake());
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);

        // 2/2 attacker — Drake (2/4) survives
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Darkslick Drake should still be alive
        harness.assertOnBattlefield(player1, "Darkslick Drake");

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Darkslick Drake"));
    }

    @Test
    @DisplayName("Exiling Darkslick Drake does not trigger a draw")
    void exilingDoesNotDraw() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new DarkslickDrake());
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().removePermanentToExile(gd, drake);

        harness.assertNotOnBattlefield(player1, "Darkslick Drake");
        harness.assertNotInGraveyard(player1, "Darkslick Drake");
        assertThat(gd.findExiledCard(drake.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Returning Darkslick Drake to hand does not trigger a draw")
    void returningToHandDoesNotDraw() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new DarkslickDrake());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, drake.getId());

        harness.assertNotOnBattlefield(player1, "Darkslick Drake");
        harness.assertInHand(player1, "Darkslick Drake");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneously dying Drakes each draw for their own controller")
    void simultaneousDeathsDrawForEachController() {
        harness.addToBattlefield(player1, new DarkslickDrake());
        harness.addToBattlefield(player2, new DarkslickDrake());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Darkslick Drake");
        harness.assertInGraveyard(player2, "Darkslick Drake");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
