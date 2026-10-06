package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanDockhand.class, Forest.class, BalduvianBears.class, Island.class})
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

    @Test
    void canTargetAlreadyTappedLand() {
        addReadyDockhand(player1);
        Permanent land = addLand(player2);
        land.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent dockhand = addReadyDockhand(player1);
        Permanent land = addLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dockhand);

        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void abilityDoesNothingWhenTargetLeavesBattlefield() {
        Permanent dockhand = addReadyDockhand(player1);
        Permanent land = addLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        Permanent replacement = addLand(player2);

        harness.passBothPriorities();

        assertThat(replacement.isTapped()).isFalse();
        assertThat(dockhand.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void islandwalkPreventsBlockingEvenWithTappedIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.tap();
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = addReadyDockhand(player1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void islandControlledOnlyByAttackerDoesNotPreventBlocking() {
        Permanent attacker = addReadyDockhand(player1);
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyDockhand(Player player) {
        return addCreatureReady(player, new RishadanDockhand());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
