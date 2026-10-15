package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenwoodSentinel.class})
class GreenwoodSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Greenwood Sentinel untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());

        declareAttackers(List.of(0));

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped creature to attack")
    void tappedSentinelCannotAttack() {
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());
        assertThat(als.canAttack(gd, sentinel, player1.getId())).isTrue();

        sentinel.tap();

        assertThat(als.canAttack(gd, sentinel, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not bypass summoning sickness")
    void summoningSickSentinelCannotAttack() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(als.canAttack(gd, sentinel, player1.getId())).isFalse();

        sentinel.setSummoningSick(false);

        assertThat(als.canAttack(gd, sentinel, player1.getId())).isTrue();
    }
}
