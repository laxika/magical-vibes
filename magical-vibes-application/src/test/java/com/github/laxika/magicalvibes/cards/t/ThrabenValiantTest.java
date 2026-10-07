package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenValiant.class})
class ThrabenValiantTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapValiant() {
        Permanent valiant = addCreatureReady(player1, new ThrabenValiant());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(valiant.isAttacking()).isTrue();
        assertThat(valiant.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowTappedValiantToAttack() {
        Permanent valiant = addCreatureReady(player1, new ThrabenValiant());
        valiant.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(valiant.isAttacking()).isFalse();
        assertThat(valiant.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowSummoningSickValiantToAttack() {
        Permanent valiant = addCreatureReady(player1, new ThrabenValiant());
        valiant.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(valiant.isAttacking()).isFalse();
        assertThat(valiant.isTapped()).isFalse();
    }
}
