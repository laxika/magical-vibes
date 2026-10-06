package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({Shriekhorn.class})
class ShriekhornTest extends BaseCardTest {


    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.castFromHand(player1, new Shriekhorn(), "{1}");
        harness.passBothPriorities();

        Permanent shriekhorn = findPermanent(player1, "Shriekhorn");
        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }


    @Test
    @DisplayName("Activating ability removes a charge counter and target player mills 2 cards")
    void activateRemovesCounterAndTargetMills() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 3);

        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(initialLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can activate three times with 3 charge counters (untapping between uses)")
    void canActivateThreeTimes() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 3);

        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        shriekhorn.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        shriekhorn.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(initialLibrarySize - 6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself to mill your own cards")
    void canTargetSelf() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 1);

        int initialLibrarySize = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(initialLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate while tapped (requires tap)")
    void cannotActivateWhileTapped() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 3);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(shriekhorn.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Tap and charge counter costs are paid before milling resolves")
    void paysCostsBeforeResolution() {
        harness.castFromHand(player1, new Shriekhorn(), "{1}");
        harness.passBothPriorities();
        Permanent shriekhorn = findPermanent(player1, "Shriekhorn");
        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(shriekhorn.isTapped()).isTrue();
        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(initialLibrarySize);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(initialLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills the only card when the target library has fewer than two cards")
    void millsShortLibrary() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 1);
        Shriekhorn libraryCard = new Shriekhorn();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(libraryCard);
        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can target an empty library and still pays the activation costs")
    void millsEmptyLibrary() {
        Permanent shriekhorn = harness.addToBattlefieldAndReturn(player1, new Shriekhorn());
        shriekhorn.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(shriekhorn.isTapped()).isTrue();
        assertThat(shriekhorn.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
