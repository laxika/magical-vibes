package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorclawMyr.class, CarapaceForger.class, WallOfTanglecord.class})
class IchorclawMyrTest extends BaseCardTest {

    @Test
    @DisplayName("When Ichorclaw Myr becomes blocked, a triggered ability is pushed onto the stack")
    void becomesBlockedPushesTriggerOntoStack() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.setAttacking(true);

        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Ichorclaw Myr")
                        && entry.getSourcePermanentId().equals(myrPerm.getId()));
    }

    @Test
    @DisplayName("Resolving becomes-blocked trigger gives +2/+2 until end of turn")
    void becomesBlockedTriggerGivesPlusTwoPlusTwo() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.setAttacking(true);

        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);
        assertThat(myrPerm.getEffectivePower()).isEqualTo(3);
        assertThat(myrPerm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Becomes-blocked trigger fires only once even with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent myrPerm = addMyrReady(player1);
        TestCards.mutableCard(myrPerm).setPower(4);
        TestCards.mutableCard(myrPerm).setToughness(4);
        myrPerm.setAttacking(true);

        addCreatureReady(player2, new CarapaceForger());
        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long myrTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Ichorclaw Myr"))
                .count();
        assertThat(myrTriggerCount).isEqualTo(1);

        harness.passBothPriorities();

        // Only +2/+2, not +4/+4
        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("+2/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent myrPerm = addMyrReady(player1);
        TestCards.mutableCard(myrPerm).setPower(4);
        TestCards.mutableCard(myrPerm).setToughness(4);
        myrPerm.setAttacking(true);

        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(0);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Unblocked Ichorclaw Myr deals poison without losing life or getting a boost")
    void unblockedCombatDealsPoison() {
        Permanent myr = addMyrReady(player1);
        myr.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(myr.getPowerModifier()).isZero();
        assertThat(myr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocked Ichorclaw Myr deals three infect counters after its trigger resolves")
    void boostedCombatDealsMinusCounters() {
        Permanent myr = addMyrReady(player1);
        myr.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfTanglecord());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(wall.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Wall of Tanglecord");
        harness.assertOnBattlefield(player1, "Ichorclaw Myr");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Blocking with Ichorclaw Myr does not trigger its boost")
    void blockingDoesNotBoostMyr() {
        Permanent attacker = addCreatureReady(player1, new CarapaceForger());
        attacker.setAttacking(true);
        Permanent myr = addMyrReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        resolveCombat();
        assertThat(myr.getPowerModifier()).isZero();
        assertThat(myr.getToughnessModifier()).isZero();
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Ichorclaw Myr");
    }

    private Permanent addMyrReady(Player player) {
        return addCreatureReady(player, new IchorclawMyr());
    }
}
