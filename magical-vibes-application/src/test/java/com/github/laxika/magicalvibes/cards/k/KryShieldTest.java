package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KryShield.class, ProdigalSorcerer.class})
class KryShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage by the target creature and gives it +0/+X")
    void preventsDamageAndBoostsToughness() {
        harness.setLife(player2, 20);
        addReadyShield(player1);
        Permanent sorcerer = addReadySorcerer(player1);

        activateShield(sorcerer);

        assertThat(sorcerer.getPowerModifier()).isZero();
        assertThat(sorcerer.getToughnessModifier()).isEqualTo(3);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage by another creature")
    void doesNotPreventDamageByAnotherCreature() {
        harness.setLife(player2, 20);
        addReadyShield(player1);
        Permanent protectedSorcerer = addReadySorcerer(player1);
        Permanent otherSorcerer = addReadySorcerer(player1);

        activateShield(protectedSorcerer);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The boost and damage prevention expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent shield = addReadyShield(player1);
        Permanent sorcerer = addReadySorcerer(player1);

        activateShield(sorcerer);
        assertThat(sorcerer.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sorcerer.getPowerModifier()).isZero();
        assertThat(sorcerer.getToughnessModifier()).isZero();
        assertThat(shield.isTapped()).isTrue();

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent shield = addReadyShield(player1);
        Permanent opponentCreature = addReadySorcerer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.isTapped()).isFalse();
    }

    private Permanent addReadyShield(Player player) {
        Permanent shield = harness.addToBattlefieldAndReturn(player, new KryShield());
        shield.setSummoningSick(false);
        return shield;
    }

    private Permanent addReadySorcerer(Player player) {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        return sorcerer;
    }

    private void activateShield(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
