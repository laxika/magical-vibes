package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberEyeWolf.class})
class EmberEyeWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent wolf = addCreatureReady(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent wolf = addCreatureReady(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(4);
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost lasts through the end step and expires during cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only the wolf that activated the ability gets the boost")
    void boostsOnlyItsSource() {
        Permanent wolf = addCreatureReady(player1, new EmberEyeWolf());
        Permanent otherWolf = addCreatureReady(player1, new EmberEyeWolf());
        Permanent opposingWolf = addCreatureReady(player2, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(otherWolf.getPowerModifier()).isZero();
        assertThat(opposingWolf.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The ability can be activated while tapped")
    void canActivateWhileTapped() {
        Permanent wolf = addCreatureReady(player1, new EmberEyeWolf());
        wolf.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated the turn the wolf enters")
    void canActivateOnEntryTurn() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste allows the wolf to attack the turn it enters")
    void hasteAllowsAttackOnEntryTurn() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new EmberEyeWolf());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wolf.isAttacking()).isTrue();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires a red mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability requires payment of the generic mana too")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new EmberEyeWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }
}
