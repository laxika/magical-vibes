package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSkybreak.class, HornedTurtle.class, Wasteland.class})
class SeekerOfSkybreakTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the Seeker")
    void activatingTapsSeeker() {
        Permanent seeker = addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(seeker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability while the Seeker is tapped")
    void cannotActivateWhileTapped() {
        Permanent seeker = addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        seeker.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target and untap itself")
    void canTargetItself() {
        Permanent seeker = addCreatureReady(player1, new SeekerOfSkybreak());

        harness.activateAbility(player1, 0, null, seeker.getId());
        harness.passBothPriorities();

        assertThat(seeker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wasteland());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        harness.addToBattlefield(player2, new HornedTurtle());
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
