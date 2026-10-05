package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterDecoy.class, GrizzlyBears.class, Quicksand.class})
class MasterDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Master Decoy")
    void activatingTapsDecoy() {
        Permanent decoy = addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(decoy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addReadyDecoy(player1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already tapped creature")
    void canTargetAlreadyTappedCreature() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyDecoy(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate the turn it enters (summoning sickness)")
    void cannotActivateWhileSummoningSick() {
        Permanent decoy = harness.addToBattlefieldAndReturn(player1, new MasterDecoy());
        decoy.setSummoningSick(true);

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when Master Decoy is already tapped")
    void cannotActivateWhenDecoyIsTapped() {
        Permanent decoy = addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        decoy.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Master Decoy can target itself")
    void canTargetItself() {
        Permanent decoy = addReadyDecoy(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, decoy.getId());

        assertThat(decoy.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(decoy.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability still resolves after Master Decoy leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent decoy = addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(decoy);
        gd.playerGraveyards.get(player1.getId()).add(decoy.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation spends exactly one white mana before resolution")
    void paysWhiteManaOnActivation() {
        addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the white activation cost")
    void cannotPayWithColorlessMana() {
        Permanent decoy = addReadyDecoy(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(decoy.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyDecoy(Player player) {
        return addCreatureReady(player, new MasterDecoy());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Quicksand());
    }
}
