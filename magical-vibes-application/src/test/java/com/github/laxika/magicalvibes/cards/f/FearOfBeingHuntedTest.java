package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfBeingHunted.class, GrizzlyBears.class})
class FearOfBeingHuntedTest extends BaseCardTest {

    @Test
    @DisplayName("Must be blocked when an able blocker exists")
    void mustBeBlockedIfAble() {
        Permanent fear = attackingCreature(new FearOfBeingHunted());
        gd.playerBattlefields.get(player1.getId()).add(fear);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unable blocker does not satisfy the requirement")
    void doesNotRequireBlockWhenNoAbleBlockerExists() {
        Permanent fear = attackingCreature(new FearOfBeingHunted());
        gd.playerBattlefields.get(player1.getId()).add(fear);
        Permanent tappedBlocker = readyCreature(player2, new GrizzlyBears());
        tappedBlocker.tap();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
