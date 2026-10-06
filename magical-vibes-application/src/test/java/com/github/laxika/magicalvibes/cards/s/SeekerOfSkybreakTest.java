package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSkybreak.class, HornedTurtle.class, Forest.class})
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
    @DisplayName("Can target a creature that is already untapped")
    void canTargetAlreadyUntappedCreature() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = addCreatureReady(player2, new HornedTurtle());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Ability still untaps its target after the Seeker leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent seeker = addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(seeker);
        gd.playerGraveyards.get(player1.getId()).add(seeker.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping a summoning-sick Seeker does not let it activate its tap ability")
    void untappingDoesNotRemoveSummoningSickness() {
        addCreatureReady(player1, new SeekerOfSkybreak());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeekerOfSkybreak());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
