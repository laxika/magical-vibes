package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForiysianInterceptor.class, GrizzlyBears.class})
class ForiysianInterceptorTest extends BaseCardTest {

    @Test
    @DisplayName("Foriysian Interceptor can block two attackers")
    void canBlockTwoAttackers() {
        Permanent interceptor = addInterceptor();
        int interceptorIndex = gd.playerBattlefields.get(player2.getId()).indexOf(interceptor);
        addAttackers(2);
        beginBlockerDeclaration();

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
        beginBlockerDeclaration();

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
        Permanent bears = addReadyCreature(player2);
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        addAttackers(2);
        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearsIndex, 0),
                new BlockerAssignment(bearsIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private Permanent addInterceptor() {
        return addReadyCreature(player2, new ForiysianInterceptor());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyCreature(player, new GrizzlyBears());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addReadyCreature(player1);
            attacker.setAttacking(true);
        }
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
