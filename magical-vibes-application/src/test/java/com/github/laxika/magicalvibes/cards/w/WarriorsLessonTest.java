package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarriorsLesson.class, TravelingPhilosopher.class})
class WarriorsLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Each of two targeted creatures draws a card when it deals combat damage")
    void bothTargetedCreaturesDraw() {
        Permanent first = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent second = addCreatureReady(player1, new TravelingPhilosopher());

        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        first.setAttacking(true);
        second.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The granted triggers expire at end of turn")
    void triggersExpireAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new TravelingPhilosopher());

        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        bears.setAttacking(true);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearsId = harness.getPermanentId(player2, "Traveling Philosopher");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One target draws one card regardless of damage, while an untargeted creature draws none")
    void onlyTargetedCreatureDraws() {
        Permanent target = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent other = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher(), new TravelingPhilosopher()));

        target.setAttacking(true);
        other.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can cast with no targets and grants no ability")
    void zeroTargets() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.<UUID>of());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));

        creature.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(WarriorsLesson.class::isInstance);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two casts grant two independent draw abilities to the same creature")
    void repeatedCastsDrawTwice() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson(), new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher(), new TravelingPhilosopher()));

        creature.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Remaining target still gains the ability when the other target leaves")
    void partiallyIllegalTargets() {
        Permanent first = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent second = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));

        second.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void damageToCreatureDoesNotDraw() {
        Permanent attacker = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent blocker = addCreatureReady(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(TravelingPhilosopher.class::isInstance);
    }

    @Test
    @DisplayName("Cannot select three creatures or the same creature twice")
    void rejectsInvalidTargetCounts() {
        Permanent first = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent second = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent third = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new WarriorsLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
