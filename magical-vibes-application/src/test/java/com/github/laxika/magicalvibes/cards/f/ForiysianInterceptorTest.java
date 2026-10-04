package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForiysianInterceptor.class, AshcoatBear.class})
class ForiysianInterceptorTest extends BaseCardTest {

    @Test
    @DisplayName("Foriysian Interceptor cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new ForiysianInterceptor());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Foriysian Interceptor can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurnBecauseOfFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ForiysianInterceptor(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Foriysian Interceptor");
    }

    @Test
    @DisplayName("Foriysian Interceptor can block two attackers")
    void canBlockTwoAttackers() {
        Permanent interceptor = addInterceptor();
        int interceptorIndex = gd.playerBattlefields.get(player2.getId()).indexOf(interceptor);
        addAttackers(2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(interceptorIndex, 0),
                new BlockerAssignment(interceptorIndex, 1)
        ));

        assertThat(interceptor.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Foriysian Interceptor cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent interceptor = addInterceptor();
        int interceptorIndex = gd.playerBattlefields.get(player2.getId()).indexOf(interceptor);
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(interceptorIndex, 0),
                new BlockerAssignment(interceptorIndex, 1),
                new BlockerAssignment(interceptorIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Foriysian Interceptor does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOtherCreatures() {
        addInterceptor();
        Permanent bears = addCreatureReady(player2, new AshcoatBear());
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearsIndex, 0),
                new BlockerAssignment(bearsIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("A summoning-sick Foriysian Interceptor can block two attackers")
    void canBlockTwoAttackersWhileSummoningSick() {
        Permanent interceptor = harness.addToBattlefieldAndReturn(player2, new ForiysianInterceptor());
        interceptor.setSummoningSick(true);
        addAttackers(2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));

        assertThat(interceptor.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("The additional-block ability does not allow a tapped Interceptor to block")
    void cannotBlockWhileTapped() {
        Permanent interceptor = addInterceptor();
        interceptor.setTapped(true);
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        )))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addInterceptor() {
        return addCreatureReady(player2, new ForiysianInterceptor());
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new AshcoatBear());
            attacker.setAttacking(true);
        }
    }
}
