package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyTerror.class, AncientBrontodon.class, GrazingWhiptail.class})
class SkyTerrorTest extends BaseCardTest {

    @Test
    void singleFlyingBlockerCannotBlock() {
        addCreatureReady(player1, new SkyTerror());
        addCreatureReady(player2, new SkyTerror());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void twoFlyingBlockersCanBlock() {
        addCreatureReady(player1, new SkyTerror());
        Permanent first = addCreatureReady(player2, new SkyTerror());
        Permanent second = addCreatureReady(player2, new SkyTerror());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void twoGroundCreaturesCannotBlock() {
        addCreatureReady(player1, new SkyTerror());
        addCreatureReady(player2, new AncientBrontodon());
        addCreatureReady(player2, new AncientBrontodon());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingAndReachBlockersCanBlockTogether() {
        addCreatureReady(player1, new SkyTerror());
        Permanent flyer = addCreatureReady(player2, new SkyTerror());
        Permanent reach = addCreatureReady(player2, new GrazingWhiptail());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(flyer.isBlocking()).isTrue();
        assertThat(reach.isBlocking()).isTrue();
    }

    @Test
    void groundCreatureCannotSupplementOneLegalBlocker() {
        addCreatureReady(player1, new SkyTerror());
        addCreatureReady(player2, new SkyTerror());
        addCreatureReady(player2, new AncientBrontodon());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBlockGroundAttackerAlone() {
        addCreatureReady(player1, new AncientBrontodon());
        Permanent blocker = addCreatureReady(player2, new SkyTerror());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void mayRemainUnblockedDespiteAvailableBlockers() {
        addCreatureReady(player1, new SkyTerror());
        Permanent first = addCreatureReady(player2, new SkyTerror());
        Permanent second = addCreatureReady(player2, new SkyTerror());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());

        assertThat(first.isBlocking()).isFalse();
        assertThat(second.isBlocking()).isFalse();
    }
}
