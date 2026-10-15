package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlpineWatchdog.class})
class AlpineWatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Alpine Watchdog untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());

        declareAttackers(List.of(0));

        assertThat(watchdog.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Alpine Watchdog to attack")
    void tappedWatchdogCannotAttack() {
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());
        watchdog.tap();

        assertThat(als.canAttack(gd, watchdog, player1.getId())).isFalse();
        assertThat(watchdog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Alpine Watchdog to attack")
    void summoningSickWatchdogCannotAttack() {
        Permanent watchdog = addCreatureReady(player1, new AlpineWatchdog());
        watchdog.setSummoningSick(true);

        assertThat(als.canAttack(gd, watchdog, player1.getId())).isFalse();
    }
}
