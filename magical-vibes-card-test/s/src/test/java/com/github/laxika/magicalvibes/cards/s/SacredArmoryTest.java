package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredArmory.class, GrizzlyBears.class, HillGiant.class})
class SacredArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0 until end of turn")
    void boostsTargetCreature() {
        harness.addToBattlefield(player1, new SacredArmory());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canBoostOpponentCreature() {
        harness.addToBattlefield(player1, new SacredArmory());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations stack and can be paid with colored mana")
    void repeatedActivationsStack() {
        Permanent armory = harness.addToBattlefieldAndReturn(player1, new SacredArmory());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(armory.isTapped()).isFalse();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isZero();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate Sacred Armory while it is tapped")
    void canActivateWhileTapped() {
        Permanent armory = harness.addToBattlefieldAndReturn(player1, new SacredArmory());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        armory.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(armory.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One mana is insufficient to activate Sacred Armory")
    void cannotActivateWithOnlyOneMana() {
        harness.addToBattlefield(player1, new SacredArmory());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent armory = harness.addToBattlefieldAndReturn(player1, new SacredArmory());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armory.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability resolves after Sacred Armory leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent armory = harness.addToBattlefieldAndReturn(player1, new SacredArmory());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(armory);
        gd.playerGraveyards.get(player1.getId()).add(armory.getCard());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
