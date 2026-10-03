package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DwarvenPony;
import com.github.laxika.magicalvibes.cards.j.JovensTools;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chandler.class, ClockworkGnomes.class, DwarvenPony.class, JovensTools.class})
class ChandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact creature")
    void destroysArtifactCreature() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Clockwork Gnomes");
        harness.assertInGraveyard(player2, "Clockwork Gnomes");
    }

    @Test
    @DisplayName("Ability taps Chandler as a cost")
    void tapsAsCost() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(findPermanent(player1, "Chandler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonArtifactCreature() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwarvenPony());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JovensTools());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Cannot activate without enough red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact creature you control")
    void destroysOwnArtifactCreature() {
        addCreatureReady(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clockwork Gnomes");
        harness.assertInGraveyard(player1, "Clockwork Gnomes");
    }

    @Test
    @DisplayName("An artifact creature can regenerate from Chandler's destruction")
    void allowsRegeneration() {
        addCreatureReady(player1, new Chandler());
        Permanent target = addCreatureReady(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Clockwork Gnomes");
        harness.assertNotInGraveyard(player2, "Clockwork Gnomes");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while Chandler has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Chandler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Clockwork Gnomes");
    }

    @Test
    @DisplayName("Cannot activate while Chandler is already tapped")
    void cannotActivateWhileTapped() {
        Permanent chandler = addCreatureReady(player1, new Chandler());
        chandler.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Clockwork Gnomes");
    }
}
