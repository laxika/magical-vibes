package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SomberwaldSpider;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbbeyGriffin.class, WalkingCorpse.class, SomberwaldSpider.class})
class AbbeyGriffinTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(griffin.isAttacking()).isTrue();
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new AbbeyGriffin());
        Permanent blocker = addCreatureReady(player2, new AbbeyGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new AbbeyGriffin());
        Permanent blocker = addCreatureReady(player2, new SomberwaldSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceKeepsAttackingGriffinUntapped() {
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());
        addCreatureReady(player2, new AbbeyGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(griffin.isAttacking()).isTrue();
        assertThat(griffin.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedCreatureToAttack() {
        Permanent griffin = addCreatureReady(player1, new AbbeyGriffin());
        griffin.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(griffin.isAttacking()).isFalse();
        assertThat(griffin.isTapped()).isTrue();
    }
}
