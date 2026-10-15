package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GrimMonolith.class)
class GrimMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Grim Monolith produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        addReadyMonolith(player1, false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Grim Monolith does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent monolith = addReadyMonolith(player1, true);

        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(monolith.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {4} untaps Grim Monolith")
    void payingFourUntapsMonolith() {
        Permanent monolith = addReadyMonolith(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Grim Monolith can be tapped again after paying to untap it")
    void canBeTappedAgainAfterUntapping() {
        Permanent monolith = addReadyMonolith(player1, false);

        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Grim Monolith's untap ability requires {4}")
    void cannotUntapWithoutFourMana() {
        addReadyMonolith(player1, true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A newly entered Grim Monolith produces mana immediately without using the stack")
    void newlyEnteredMonolithProducesManaImmediately() {
        Permanent monolith = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());

        harness.tapPermanent(player1, 0);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The untap ability uses the stack and untaps only its source")
    void untapAbilityResolvesOnStackAndOnlyUntapsSource() {
        Permanent monolith = addReadyMonolith(player1, true);
        Permanent otherMonolith = addReadyMonolith(player1, true);
        Permanent opponentsMonolith = addReadyMonolith(player2, true);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(otherMonolith.isTapped()).isTrue();
        assertThat(opponentsMonolith.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Grim Monolith cannot produce mana again")
    void cannotTapTappedMonolithForMana() {
        addReadyMonolith(player1, true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyMonolith(Player player, boolean tapped) {
        Permanent monolith = harness.addToBattlefieldAndReturn(player, new GrimMonolith());
        monolith.setSummoningSick(false);
        if (tapped) {
            monolith.tap();
        }
        return monolith;
    }

}
