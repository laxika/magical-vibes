package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SendToSleep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JhessianThief.class, SerraAngel.class, SendToSleep.class})
class JhessianThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        thief.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches a player")
    void noDrawWhenBlocked() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        thief.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Prowess resolves before the spell, stacks, and expires at end of turn")
    void prowessStacksAndExpires() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        harness.setHand(player1, List.of(new SendToSleep(), new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.castInstant(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotTriggerProwess() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SendToSleep()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, List.of());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature spell does not trigger prowess")
    void creatureSpellDoesNotTriggerProwess() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        harness.setHand(player1, List.of(new JhessianThief()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Increased combat damage still draws exactly one card")
    void boostedCombatDamageDrawsOnlyOneCard() {
        Permanent thief = addCreatureReady(player1, new JhessianThief());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of());
        resolveAllTriggers();
        thief.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The draw goes to the attacking Thief's controller even after it leaves")
    void drawSurvivesSourceLeavingAndUsesItsController() {
        Permanent thief = addCreatureReady(player2, new JhessianThief());
        thief.setAttacking(true);
        harness.setLibrary(player2, List.of(new JhessianThief()));
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();
        int defenderHandSize = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(thief);
        gd.playerGraveyards.get(player2.getId()).add(thief.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(defenderHandSize);
    }
}
