package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FyndhornBrownieTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the Brownie")
    void activatingTapsBrownie() {
        Permanent brownie = addReadyBrownie(player1);
        Permanent target = addReadyCreature(player2);
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(brownie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addReadyBrownie(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        addReadyBrownie(player1);
        Permanent ownCreature = addReadyCreature(player1);
        ownCreature.tap();
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyBrownie(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyBrownie(player1);
        Permanent land = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        addBrownieMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyBrownie(player1);
        Permanent target = addReadyCreature(player2);
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private void addBrownieMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyBrownie(Player player) {
        Permanent perm = new Permanent(new FyndhornBrownie());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
