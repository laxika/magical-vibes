package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirdropAeronauts.class, AegisAutomaton.class})
class AirdropAeronautsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 5 life if a permanent you controlled left the battlefield this turn")
    void gainsLifeAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.castFromHand(player1, new AirdropAeronauts(), "{3}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Does not gain life when only an opponent's permanent left the battlefield")
    void doesNotGainLifeAfterOpponentPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.castFromHand(player1, new AirdropAeronauts(), "{3}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not gain life if no permanent left the battlefield")
    void doesNotGainLifeWithoutRevolt() {
        harness.castFromHand(player1, new AirdropAeronauts(), "{3}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Revolt does not trigger retroactively when a permanent leaves after entry")
    void departureAfterEntryDoesNotEnableRevolt() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.enterBattlefieldAndReturn(player1, new AirdropAeronauts());

        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, support));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple departures still give only 5 life and the trigger survives its source leaving")
    void gainsLifeOnceEvenIfSourceLeavesBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        Permanent aeronauts = harness.enterBattlefieldAndReturn(player1, new AirdropAeronauts());

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aeronauts));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
