package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlpineWatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Alpine Watchdog untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));

        assertThat(watchdog.isTapped()).isFalse();
    }
}
