package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandbarCrocodile.class})
class SandbarCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing phases Sandbar Crocodile out during its controller's untap step")
    void phasesOutDuringControllersUntapStep() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crocodile);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);
    }

    @Test
    @DisplayName("Phased-out Sandbar Crocodile returns during its controller's next untap step")
    void phasesBackInDuringNextControllersUntapStep() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());

        advanceToUpkeep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(crocodile);
    }
    @Test
    @DisplayName("A tapped Crocodile phases out before untapping and untaps when it phases back in")
    void phasesBeforeUntapping() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());
        crocodile.tap();

        harness.performUntapStep(player1);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);
        assertThat(crocodile.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(crocodile.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);
        assertThat(crocodile.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Phasing preserves the Crocodile's counters and does not move its card to another zone")
    void preservesCountersAcrossPhasing() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());
        crocodile.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.performUntapStep(player1);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);
        assertThat(crocodile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Sandbar Crocodile");
        harness.assertNotInHand(player1, "Sandbar Crocodile");

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);
        assertThat(crocodile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("One Crocodile phases in while another phases out in the same untap step")
    void phasesInAndOutSimultaneously() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());
        harness.performUntapStep(player1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(second).doesNotContain(first);
    }
}
