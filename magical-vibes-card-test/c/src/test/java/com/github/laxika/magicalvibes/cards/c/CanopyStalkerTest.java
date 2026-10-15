package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FinishingBlow;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanopyStalker.class, DoomBlade.class, GrizzlyBears.class, FinishingBlow.class})
class CanopyStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Must be blocked when an able blocker exists")
    void mustBeBlockedIfAble() {
        Permanent stalker = addCreatureReady(player1, new CanopyStalker());
        stalker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gains one life for each creature that died this turn when it dies")
    void gainsLifeForEachCreatureThatDiedThisTurn() {
        harness.setLife(player1, 20);
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new CanopyStalker());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, stalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("An available blocker cannot evade the requirement by blocking another attacker")
    void cannotAssignOnlyBlockerToOrdinaryAttacker() {
        addCreatureReady(player1, new CanopyStalker()).setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new CanopyStalker());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped creature does not have to block")
    void noBlockRequiredWhenOnlyDefenderIsTapped() {
        addCreatureReady(player1, new CanopyStalker()).setAttacking(true);
        addCreatureReady(player2, new CanopyStalker()).tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The death trigger includes creatures that die in response")
    void countsDeathsAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new CanopyStalker());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FinishingBlow(), new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0, stalker.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.castAndResolveInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
