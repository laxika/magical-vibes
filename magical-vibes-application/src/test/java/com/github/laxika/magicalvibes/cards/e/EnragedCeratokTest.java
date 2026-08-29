package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnragedCeratokTest extends BaseCardTest {

    @Test
    @DisplayName("Enraged Ceratok can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        Permanent ceratok = attackingCeratok();
        gd.playerBattlefields.get(player1.getId()).add(ceratok);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enraged Ceratok can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPower3OrGreater() {
        Permanent ceratok = attackingCeratok();
        gd.playerBattlefields.get(player1.getId()).add(ceratok);

        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    private Permanent attackingCeratok() {
        Permanent ceratok = new Permanent(new EnragedCeratok());
        ceratok.setSummoningSick(false);
        ceratok.setAttacking(true);
        return ceratok;
    }
}
