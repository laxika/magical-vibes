package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PhyrexianIronfoot.class)
class PhyrexianIronfootTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Ironfoot does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent ironfoot = addCreatureReady(player1, new PhyrexianIronfoot());
        ironfoot.tap();

        advanceToUpkeep(player1);

        assertThat(ironfoot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying generic and snow mana untaps Phyrexian Ironfoot")
    void payingGenericAndSnowManaUntapsIt() {
        Permanent ironfoot = addCreatureReady(player1, new PhyrexianIronfoot());
        ironfoot.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ironfoot.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regular mana cannot pay Phyrexian Ironfoot's snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new PhyrexianIronfoot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Colored snow mana can pay both activation symbols")
    void coloredSnowManaPaysEntireActivationCost() {
        Permanent ironfoot = addCreatureReady(player1, new PhyrexianIronfoot());
        ironfoot.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ironfoot.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(ironfoot.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Phyrexian Ironfoot can activate its untap ability")
    void summoningSicknessDoesNotPreventActivation() {
        Permanent ironfoot = harness.addToBattlefieldAndReturn(player1, new PhyrexianIronfoot());
        ironfoot.setSummoningSick(true);
        ironfoot.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ironfoot.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability untaps only the Ironfoot that activated it")
    void activationUntapsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new PhyrexianIronfoot());
        Permanent other = addCreatureReady(player1, new PhyrexianIronfoot());
        Permanent opponent = addCreatureReady(player2, new PhyrexianIronfoot());
        source.tap();
        other.tap();
        opponent.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability can be activated while Ironfoot is already untapped")
    void canActivateWhileUntapped() {
        Permanent ironfoot = addCreatureReady(player1, new PhyrexianIronfoot());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ironfoot.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
