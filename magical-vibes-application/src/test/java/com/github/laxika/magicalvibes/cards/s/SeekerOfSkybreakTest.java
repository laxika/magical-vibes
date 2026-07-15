package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeekerOfSkybreakTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the Seeker")
    void activatingTapsSeeker() {
        Permanent seeker = addReadySeeker(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(seeker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addReadySeeker(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        addReadySeeker(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadySeeker(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent land = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SeekerOfSkybreak());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadySeeker(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadySeeker(Player player) {
        Permanent perm = new Permanent(new SeekerOfSkybreak());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
