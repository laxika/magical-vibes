package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelimTorsDarts.class, GarrukWildspeaker.class})
class TelimTorsDartsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void dealsOneDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability taps the artifact, so it cannot be activated twice")
    void abilityTapsTheArtifact() {
        harness.setLife(player2, 20);
        Permanent darts = addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(darts.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability deals 1 damage to target planeswalker")
    void dealsOneDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activation requires two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent darts = addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(darts.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate on the turn it enters and target its controller")
    void canActivateImmediatelyTargetingController() {
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new TelimTorsDarts());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target an ordinary artifact")
    void cannotTargetArtifact() {
        Permanent darts = addReadyDarts(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TelimTorsDarts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(darts.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDarts(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TelimTorsDarts());
        perm.setSummoningSick(false);
        return perm;
    }
}
