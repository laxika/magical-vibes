package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WanderingMageTest extends BaseCardTest {

    private Permanent addMageReady() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new WanderingMage());
        harness.forceActivePlayer(player1);
        return mage;
    }

    @Test
    @DisplayName("{W}, Pay 1 life shields target creature for 2 and costs 1 life")
    void whiteAbilityShieldsCreatureAndPaysLife() {
        addMageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The white ability cannot target a player")
    void whiteAbilityCannotTargetPlayer() {
        addMageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{U} shields a Cleric or Wizard creature for 1")
    void blueAbilityShieldsClericOrWizard() {
        Permanent mage = addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, mage.getId());
        harness.passBothPriorities();

        assertThat(mage.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The blue ability cannot target a creature that is neither Cleric nor Wizard")
    void blueAbilityRejectsOtherCreatures() {
        addMageReady();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{B} plus a -1/-1 counter shields target player for 2")
    void blackAbilityShieldsPlayer() {
        Permanent mage = addMageReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(mage.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability cannot target a creature")
    void blackAbilityCannotTargetCreature() {
        addMageReady();
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
