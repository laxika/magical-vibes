package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteepleCreeper.class})
class SteepleCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives Steeple Creeper flying until end of turn")
    void grantsFlying() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SteepleCreeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Four generic mana cannot replace the blue activation cost")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new SteepleCreeper());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Flying is granted on resolution only to the activating Creeper")
    void grantsFlyingOnlyToSourceOnResolution() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SteepleCreeper());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Creeper can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SteepleCreeper());
        creeper.setSummoningSick(true);
        creeper.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.FLYING)).isTrue();
        assertThat(creeper.isTapped()).isTrue();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
