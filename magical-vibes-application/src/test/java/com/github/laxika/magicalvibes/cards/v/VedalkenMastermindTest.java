package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenMastermind.class, GrizzlyBears.class, AngelicChorus.class, Island.class})
class VedalkenMastermindTest extends BaseCardTest {

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting own creature")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Vedalken Mastermind")
    void activatingTapsMastermind() {
        Permanent mastermind = addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(mastermind.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Resolving: bounce own creature =====

    @Test
    @DisplayName("Resolving returns own creature to hand")
    void resolvingReturnsOwnCreatureToHand() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounced creature does not go to graveyard")
    void bouncedCreatureDoesNotGoToGraveyard() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Resolving: bounce own enchantment =====

    @Test
    @DisplayName("Resolving returns own enchantment to hand")
    void resolvingReturnsOwnEnchantmentToHand() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInHand(player1, "Angelic Chorus");
    }

    // ===== Resolving: bounce own land =====

    @Test
    @DisplayName("Resolving returns own land to hand")
    void resolvingReturnsOwnLandToHand() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    // ===== Can bounce itself =====

    @Test
    @DisplayName("Can bounce itself")
    void canBounceItself() {
        Permanent mastermind = addCreatureReady(player1, new VedalkenMastermind());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, mastermind.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vedalken Mastermind");
        harness.assertInHand(player1, "Vedalken Mastermind");
    }

    // ===== Cannot activate without mana =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent mastermind = addCreatureReady(player1, new VedalkenMastermind());
        mastermind.tap();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate ability with summoning sickness (creature)")
    void cannotActivateWithSummoningSickness() {
        Permanent mastermind = addCreatureReady(player1, new VedalkenMastermind());
        mastermind.setSummoningSick(true);

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability fizzles if target changes controller before resolution")
    void fizzlesIfTargetChangesController() {
        addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Target is no longer controlled by player1 when the ability resolves.
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(target);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        Permanent mastermind = addCreatureReady(player1, new VedalkenMastermind());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
        assertThat(mastermind.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}

