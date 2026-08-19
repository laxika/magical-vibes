package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VraskaBetrayalsStingTest extends BaseCardTest {

    @Test
    @DisplayName("0 draws, loses life, and proliferates a loyalty counter")
    void zeroDrawsLosesLifeAndProliferates() {
        Permanent vraska = addReadyVraska(player1, 6);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(vraska.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-2 turns a creature into a Treasure and grants its mana ability")
    void minusTwoTurnsCreatureIntoTreasure() {
        addReadyVraska(player1, 6);
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, elves)).isTrue();
        assertThat(gqs.isCreature(gd, elves)).isFalse();
        assertThat(elves.getGrantedSubtypes()).contains(CardSubtype.TREASURE);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elves);
    }

    @Test
    @DisplayName("-2 cannot target a noncreature")
    void minusTwoCannotTargetNoncreature() {
        addReadyVraska(player1, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, addReadyVraska(player2, 6).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-9 gives a target player enough poison counters to reach nine")
    void minusNineFillsPoisonCountersToNine() {
        addReadyVraska(player1, 9);
        gd.playerPoisonCounters.put(player2.getId(), 4);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("-9 does not add poison counters to a player already at nine")
    void minusNineDoesNothingAtNinePoisonCounters() {
        addReadyVraska(player1, 9);
        gd.playerPoisonCounters.put(player2.getId(), 9);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(9);
    }

    private Permanent addReadyVraska(Player player, int loyalty) {
        Permanent perm = new Permanent(new VraskaBetrayalsSting());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
