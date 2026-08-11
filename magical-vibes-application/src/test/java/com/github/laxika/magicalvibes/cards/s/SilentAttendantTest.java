package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SilentAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when the ability resolves and taps")
    void gainsLifeAndTaps() {
        Permanent attendant = addReadyAttendant();

        harness.activateAbility(player1, 0, null, null);

        assertThat(attendant.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addReadyAttendant() {
        Permanent attendant = new Permanent(new SilentAttendant());
        attendant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attendant);
        return attendant;
    }
}
