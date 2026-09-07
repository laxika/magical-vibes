package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctuaryWall.class, GrizzlyBears.class, Forest.class})
class SanctuaryWallTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void tapsTargetAndAcceptingPutsStunCountersOnBothCreatures() {
        Permanent wall = addReadyWall(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(wall.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void decliningDoesNotPutStunCountersOnEitherCreature() {
        Permanent wall = addReadyWall(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(wall.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void rejectsNonCreatureTarget() {
        addReadyWall(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addReadyWall(Player player) {
        Permanent wall = harness.addToBattlefieldAndReturn(player, new SanctuaryWall());
        wall.setSummoningSick(false);
        return wall;
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
