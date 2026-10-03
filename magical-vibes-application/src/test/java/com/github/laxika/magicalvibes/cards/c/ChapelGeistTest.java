package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
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

@CardUsed({ChapelGeist.class, WalkingCorpse.class, AbbeyGriffin.class, SomberwaldSpider.class})
class ChapelGeistTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new ChapelGeist());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByCreatureWithFlying() {
        addCreatureReady(player1, new ChapelGeist());
        Permanent blocker = addCreatureReady(player2, new AbbeyGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new ChapelGeist());
        Permanent blocker = addCreatureReady(player2, new SomberwaldSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new WalkingCorpse());
        Permanent blocker = addCreatureReady(player2, new ChapelGeist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
