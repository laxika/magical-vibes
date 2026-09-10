package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlySentinel.class, LongbowArcher.class, Warthog.class})
class GhostlySentinelTest extends BaseCardTest {

    @Test
    void flyingAndVigilanceWork() {
        Permanent sentinel = addCreatureReady(player1, new GhostlySentinel());
        addCreatureReady(player2, new Warthog());

        declareAttackers(player1, List.of(0));

        assertThat(sentinel.isTapped()).isFalse();

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureWithReachCanBlock() {
        Permanent sentinel = addCreatureReady(player1, new GhostlySentinel());
        Permanent archer = addCreatureReady(player2, new LongbowArcher());
        sentinel.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, sentinel))));

        assertThat(archer.isBlocking()).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
