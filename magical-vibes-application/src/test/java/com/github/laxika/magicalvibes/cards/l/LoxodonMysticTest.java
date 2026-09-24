package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonMystic.class, MyrMoonvessel.class, DarksteelIngot.class})
class LoxodonMysticTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new LoxodonMystic(), "{3}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new LoxodonMystic(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Loxodon Mystic");
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Loxodon Mystic")
    void activatingTapsMystic() {
        Permanent mystic = addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(mystic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTarget() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent ownMyr = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownMyr.getId());
        harness.passBothPriorities();

        assertThat(ownMyr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Loxodon Mystic is tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}

