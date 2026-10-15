package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightKeeper.class})
class BlightKeeperTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    private void addReadyBlightKeeper() {
        addCreatureReady(player1, new BlightKeeper());
    }

    @Test
    @DisplayName("Activating ability sacrifices Blight Keeper and puts drain on stack")
    void activateAbilitySacrificesAndPutsOnStack() {
        addReadyBlightKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Blight Keeper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability causes target opponent to lose 4 life and controller gains 4 life")
    void drainsOpponent() {
        addReadyBlightKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 4);
    }

    @Test
    @DisplayName("Blight Keeper goes to graveyard after sacrifice")
    void goesToGraveyardAfterSacrifice() {
        addReadyBlightKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blight Keeper");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyBlightKeeper();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target self with the ability (opponent only)")
    void cannotTargetSelf() {
        addReadyBlightKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BlightKeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Blight Keeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new BlightKeeper()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Blight Keeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlackRequirementWithOnlyGenericMana() {
        addReadyBlightKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Blight Keeper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void otherPlayerCanActivateAndReceivesLifeGain() {
        addCreatureReady(player2, new BlightKeeper());
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, player1.getId());

        harness.assertInGraveyard(player2, "Blight Keeper");
        harness.assertLife(player1, STARTING_LIFE);
        harness.assertLife(player2, STARTING_LIFE);
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE - 4);
        harness.assertLife(player2, STARTING_LIFE + 4);
    }
}
