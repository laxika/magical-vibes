package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SilentAttendant.class)
class SilentAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when the ability resolves and taps")
    void gainsLifeAndTaps() {
        Permanent attendant = addCreatureReady(player1, new SilentAttendant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(attendant.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The ability cannot be activated while Silent Attendant has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new SilentAttendant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(attendant.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the controller gains life")
    void onlyControllerGainsLife() {
        Permanent attendant = addCreatureReady(player1, new SilentAttendant());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(attendant.isTapped()).isTrue();
    }
}
