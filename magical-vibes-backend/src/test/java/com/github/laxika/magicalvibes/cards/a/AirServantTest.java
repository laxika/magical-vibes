package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AirServantTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature with flying")
    void activatingPutsOnStack() {
        addReadyServant(player1);
        Permanent flyingTarget = addReadyFlyer(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, flyingTarget.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(flyingTarget.getId());
    }

    @Test
    @DisplayName("Resolving ability taps target creature with flying")
    void resolvingTapsTarget() {
        addReadyServant(player1);
        Permanent flyingTarget = addReadyFlyer(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, flyingTarget.getId());
        harness.passBothPriorities();

        assertThat(flyingTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own creature with flying")
    void canTapOwnFlyer() {
        addReadyServant(player1);
        Permanent ownFlyer = addReadyFlyer(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, ownFlyer.getId());
        harness.passBothPriorities();

        assertThat(ownFlyer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target creature without flying")
    void cannotTargetNonFlyer() {
        addReadyServant(player1);
        Permanent bears = addReadyBears(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyServant(player1);
        Permanent flyingTarget = addReadyFlyer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyingTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability does not require tap so can be activated multiple times")
    void canActivateMultipleTimes() {
        addReadyServant(player1);
        Permanent flyer1 = addReadyFlyer(player2);
        Permanent flyer2 = addReadyFlyer(player2);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, flyer1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, flyer2.getId());
        harness.passBothPriorities();

        assertThat(flyer1.isTapped()).isTrue();
        assertThat(flyer2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyServant(player1);
        Permanent flyingTarget = addReadyFlyer(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, flyingTarget.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyServant(Player player) {
        AirServant card = new AirServant();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyFlyer(Player player) {
        AirElemental card = new AirElemental();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
