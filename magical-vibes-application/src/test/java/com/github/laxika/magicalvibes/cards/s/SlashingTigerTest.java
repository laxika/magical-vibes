package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlashingTiger.class, ShuFootSoldiers.class})
class SlashingTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates one becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent tiger = addCreatureReady(player1, new SlashingTiger());
        tiger.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(tiger.getId());
    }

    @Test
    @DisplayName("When blocked Slashing Tiger gets +2/+2 until end of turn")
    void whenBlockedGetsPlusTwoPlusTwo() {
        Permanent tiger = addCreatureReady(player1, new SlashingTiger());
        tiger.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(tiger.getPowerModifier()).isEqualTo(2);
        assertThat(tiger.getToughnessModifier()).isEqualTo(2);
        assertThat(tiger.getEffectivePower()).isEqualTo(5);
        assertThat(tiger.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple blockers still grant only a single +2/+2 (not per blocker)")
    void multipleBlockersGrantSingleBoost() {
        Permanent tiger = addCreatureReady(player1, new SlashingTiger());
        tiger.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(tiger.getPowerModifier()).isEqualTo(2);
        assertThat(tiger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent tiger = addCreatureReady(player1, new SlashingTiger());
        tiger.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(tiger.getPowerModifier()).isZero();
        assertThat(tiger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocked boost expires at the end of the turn")
    void blockedBoostExpiresAtEndOfTurn() {
        Permanent tiger = addCreatureReady(player1, new SlashingTiger());
        tiger.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(tiger.getPowerModifier()).isEqualTo(2);
        assertThat(tiger.getToughnessModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(tiger.getPowerModifier()).isZero();
        assertThat(tiger.getToughnessModifier()).isZero();
    }
}
