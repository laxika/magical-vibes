package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictoryChimes.class, SolRing.class})
class VictoryChimesTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen player receives one colorless mana")
    void chosenPlayerReceivesColorlessMana() {
        harness.addToBattlefield(player1, new VictoryChimes());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Victory Chimes untaps during another player's untap step")
    void untapsDuringOtherPlayersUntapStep() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new VictoryChimes());
        chimes.tap();

        harness.performUntapStep(player2);

        assertThat(chimes.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller can choose themselves and mana is produced without the stack")
    void controllerCanReceiveManaImmediately() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new VictoryChimes());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(chimes.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Victory Chimes cannot activate its mana ability")
    void tappedChimesCannotActivate() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new VictoryChimes());
        chimes.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Only Victory Chimes untaps among its controller's artifacts during an opponent's untap step")
    void doesNotUntapOtherArtifacts() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new VictoryChimes());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        chimes.tap();
        ring.tap();

        harness.performUntapStep(player2);

        assertThat(chimes.isTapped()).isFalse();
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Victory Chimes also untaps normally during its controller's untap step")
    void untapsDuringControllersUntapStep() {
        Permanent chimes = harness.addToBattlefieldAndReturn(player1, new VictoryChimes());
        chimes.tap();

        harness.performUntapStep(player1);

        assertThat(chimes.isTapped()).isFalse();
    }
}
