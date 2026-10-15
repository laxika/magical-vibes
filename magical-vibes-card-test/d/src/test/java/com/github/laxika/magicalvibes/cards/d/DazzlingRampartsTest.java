package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DazzlingRamparts.class, AlpineGrizzly.class})
class DazzlingRampartsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps target creature")
    void resolvingTapsTarget() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Dazzling Ramparts and consumes mana")
    void activatingTapsSelfAndConsumesMana() {
        Permanent ramparts = addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(ramparts.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addReadyRamparts(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Defender prevents Dazzling Ramparts from attacking")
    void defenderPreventsAttacking() {
        Permanent ramparts = addReadyRamparts(player1);
        Permanent bear = addReadyBears(player1);

        assertThat(als.canAttack(gd, bear, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, ramparts, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent ramparts = addReadyRamparts(player1);
        ramparts.setSummoningSick(true);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ramparts.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost when already tapped")
    void cannotActivateWhileTapped() {
        Permanent ramparts = addReadyRamparts(player1);
        ramparts.tap();
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot replace the white mana requirement")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature remains a legal target")
    void canTargetTappedCreature() {
        addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("The ability resolves after Dazzling Ramparts leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent ramparts = addReadyRamparts(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ramparts);
        gd.playerGraveyards.get(player1.getId()).add(ramparts.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dazzling Ramparts can target itself even though it taps to pay the cost")
    void canTargetItself() {
        Permanent ramparts = addReadyRamparts(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, ramparts.getId());
        harness.passBothPriorities();

        assertThat(ramparts.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyRamparts(Player player) {
        return addCreatureReady(player, new DazzlingRamparts());
    }

    private Permanent addReadyBears(Player player) {
        return addCreatureReady(player, new AlpineGrizzly());
    }
}
