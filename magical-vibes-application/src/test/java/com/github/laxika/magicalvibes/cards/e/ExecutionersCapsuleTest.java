package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.t.TidehollowStrix;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExecutionersCapsule.class, CylianElf.class, Island.class, DregscapeZombie.class,
        EsperBattlemage.class, TidehollowStrix.class})
class ExecutionersCapsuleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Executioner's Capsule and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Executioner's Capsule");
        harness.assertInGraveyard(player1, "Executioner's Capsule");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Executioner's Capsule");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability destroys target nonblack creature")
    void resolvingAbilityDestroysTargetCreature() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs black)")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate if Executioner's Capsule is already tapped")
    void cannotActivateWhenTapped() {
        Permanent capsule = addReadyCapsule(player1);
        capsule.tap();
        Permanent target = addCreatureReady(player2, new CylianElf());
        addCapsuleMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        addReadyCapsule(player1);
        // Valid target so the ability is activatable at all
        addCreatureReady(player1, new CylianElf());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new DregscapeZombie());
        addCapsuleMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyCapsule(player1);
        addCreatureReady(player1, new CylianElf());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        addCapsuleMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.getPermanentRemovalService().removePermanentToExile(gd, target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a multicolored creature that is black")
    void cannotTargetMulticoloredBlackCreature() {
        addReadyCapsule(player1);
        addCreatureReady(player1, new CylianElf());
        Permanent target = addCreatureReady(player2, new TidehollowStrix());
        addCapsuleMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a nonblack artifact creature even with black in its color identity")
    void destroysNonblackArtifactCreature() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new EsperBattlemage());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Esper Battlemage");
        harness.assertInGraveyard(player2, "Esper Battlemage");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the Capsule's controller")
    void destroysOwnCreature() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player1, new CylianElf());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Target gaining black before resolution makes the ability fizzle")
    void fizzlesIfTargetBecomesBlack() {
        addReadyCapsule(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.getGrantedColors().add(CardColor.BLACK);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cylian Elf");
        harness.assertNotInGraveyard(player2, "Cylian Elf");
        harness.assertInGraveyard(player1, "Executioner's Capsule");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can activate a Capsule on the turn it enters the battlefield")
    void canActivateFreshlyCastCapsule() {
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.castFromHand(player1, new ExecutionersCapsule(), "{B}");
        harness.passBothPriorities();
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Executioner's Capsule");
        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    private void addCapsuleMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyCapsule(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ExecutionersCapsule());
    }
}
