package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabornGrenadier.class})
class AlabornGrenadierTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent grenadier = addCreatureReady(player1, new AlabornGrenadier());

        declareAttackers(List.of(0));

        assertThat(grenadier.isTapped()).isFalse();
    }

    @Test
    void vigilanceKeepsOpponentControlledAttackerUntapped() {
        Permanent grenadier = addCreatureReady(player2, new AlabornGrenadier());
        addCreatureReady(player1, new AlabornGrenadier());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(grenadier.isAttacking()).isTrue();
        assertThat(grenadier.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedCreatureToAttack() {
        Permanent grenadier = addCreatureReady(player1, new AlabornGrenadier());
        grenadier.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grenadier.isAttacking()).isFalse();
        assertThat(grenadier.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotBypassSummoningSickness() {
        Permanent grenadier = addCreatureReady(player1, new AlabornGrenadier());
        grenadier.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grenadier.isAttacking()).isFalse();
        assertThat(grenadier.isTapped()).isFalse();
    }
}
