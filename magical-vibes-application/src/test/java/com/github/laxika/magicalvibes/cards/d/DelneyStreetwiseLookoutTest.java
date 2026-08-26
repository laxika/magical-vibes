package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelneyStreetwiseLookout.class, GrizzlyBears.class, HillGiant.class, MentorOfTheMeek.class})
class DelneyStreetwiseLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with power 2 or less cannot be blocked by power 3 or greater")
    void smallCreaturesCannotBeBlockedByLargeCreatures() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent largeBlocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with power 3 or greater");
        assertThat(largeBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with power 2 can block a small creature")
    void powerTwoCreatureCanBlockSmallCreature() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Delney makes a qualifying creature's triggered ability trigger twice")
    void doublesQualifyingCreatureTrigger() {
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        addCreatureReady(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }
}
