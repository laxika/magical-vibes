package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeditsDragoons.class})
class JeditsDragoonsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and its controller gains 4 life")
    void entersAndControllerGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        harness.castFromHand(player1, new JeditsDragoons(), "{5}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The other player's entry trigger gives life only to that player")
    void otherControllerGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new JeditsDragoons(), "{5}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Entering without being cast still triggers the life gain")
    void enteringWithoutCastingGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new JeditsDragoons());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Vigilance keeps it untapped when it attacks")
    void vigilanceKeepsItUntappedWhenAttacking() {
        var dragoons = addCreatureReady(player1, new JeditsDragoons());

        declareAttackers(List.of(0));

        assertThat(dragoons.isTapped()).isFalse();
    }
}
