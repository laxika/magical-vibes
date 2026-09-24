package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanDockhand.class, Forest.class, BalduvianBears.class})
class RishadanDockhandTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Dockhand and consumes one mana")
    void activationPaysCostAndTapsSource() {
        Permanent dockhand = addReadyDockhand(player1);
        Permanent targetLand = addLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());

        GameData gameData = harness.getGameData();
        assertThat(dockhand.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Resolving the ability taps the target land")
    void resolvingAbilityTapsTargetLand() {
        addReadyDockhand(player1);
        Permanent targetLand = addLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target an own land")
    void canTargetOwnLand() {
        addReadyDockhand(player1);
        Permanent targetLand = addLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-land")
    void cannotTargetNonLand() {
        addReadyDockhand(player1);
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        addReadyDockhand(player1);
        Permanent targetLand = addLand(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability cannot be activated while Dockhand is tapped")
    void cannotActivateWhenTapped() {
        Permanent dockhand = addReadyDockhand(player1);
        Permanent targetLand = addLand(player2);
        dockhand.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The ability cannot be activated with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent dockhand = harness.addToBattlefieldAndReturn(player1, new RishadanDockhand());
        Permanent targetLand = addLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        assertThat(dockhand.isTapped()).isFalse();
    }

    private Permanent addReadyDockhand(Player player) {
        Permanent dockhand = addCreatureReady(player, new RishadanDockhand());
        dockhand.setSummoningSick(false);
        return dockhand;
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
