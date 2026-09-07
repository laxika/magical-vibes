package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NessianBoar.class, GrizzlyBears.class})
class NessianBoarTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Nessian Boar")
    void allAbleCreaturesMustBlock() {
        Permanent boar = addAttackingCreature(player1, new NessianBoar());
        Permanent blocker1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent blocker2 = addReadyCreature(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gqs.isBlockedByAnyCreature(gd, boar)).isTrue();
        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Each blocking creature's controller draws a card")
    void eachBlockingCreatureControllerDraws() {
        addAttackingCreature(player1, new NessianBoar());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
        int player1HandSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSizeBefore);
    }

    @Test
    @DisplayName("Tapped creatures are not required to block Nessian Boar")
    void tappedCreaturesAreNotRequiredToBlock() {
        addAttackingCreature(player1, new NessianBoar());
        addReadyCreature(player2, new GrizzlyBears());
        Permanent tappedBlocker = addReadyCreature(player2, new GrizzlyBears());
        tappedBlocker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player,
                                           com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
