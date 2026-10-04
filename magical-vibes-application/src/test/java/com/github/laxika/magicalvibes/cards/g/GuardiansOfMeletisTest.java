package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ScabClanBerserker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansOfMeletis.class, ScabClanBerserker.class})
class GuardiansOfMeletisTest extends BaseCardTest {

    @Test
    void defenderPreventsAttackingEvenAfterSummoningSicknessEnds() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfMeletis());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(guardians.isAttacking()).isFalse();
        assertThat(guardians.isTapped()).isFalse();
    }

    @Test
    void defenderAllowsBlockingWhileSummoningSick() {
        addCreatureReady(player1, new ScabClanBerserker());
        Permanent guardians = addCreatureReady(player2, new GuardiansOfMeletis());
        guardians.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(guardians.isBlocking()).isTrue();
        assertThat(guardians.isTapped()).isFalse();
    }
}
