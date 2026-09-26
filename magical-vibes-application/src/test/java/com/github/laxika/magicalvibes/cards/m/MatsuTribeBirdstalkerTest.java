package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FiddleheadKami;
import com.github.laxika.magicalvibes.cards.s.ShinenOfFlightsWings;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatsuTribeBirdstalker.class, FiddleheadKami.class, ShinenOfFlightsWings.class})
class MatsuTribeBirdstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksCreature() {
        addBirdstalkerReady(player1);
        Permanent blocker = addCreatureReady(player2, new FiddleheadKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock a creature")
    void unblockedDamageDoesNotTapCreature() {
        addBirdstalkerReady(player1);
        Permanent creature = addCreatureReady(player2, new FiddleheadKami());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Green activation lets this creature block a creature with flying until end of turn")
    void activationAllowsBlockingFlyingCreature() {
        addBirdstalkerReady(player1);
        Permanent flyer = addCreatureReady(player2, new ShinenOfFlightsWings());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Green activation grants reach until end of turn")
    void activationGrantsReachUntilEndOfTurn() {
        Permanent birdstalker = addBirdstalkerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, birdstalker, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, birdstalker, Keyword.REACH)).isFalse();
    }

    private Permanent addBirdstalkerReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new MatsuTribeBirdstalker());
    }
}
