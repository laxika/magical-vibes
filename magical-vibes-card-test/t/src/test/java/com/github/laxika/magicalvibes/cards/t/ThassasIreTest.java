package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DesertersQuarters;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThassasIre.class, PensiveMinotaur.class, DesertersQuarters.class})
class ThassasIreTest extends BaseCardTest {

    @Test
    void tapsAnUntappedTargetCreature() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapsATappedTargetCreature() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        target.tap();
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        addReadyIre();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DesertersQuarters());
        addIreMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void activationDoesNotTapTheSource() {
        Permanent ire = addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(ire.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canActivateWhileSourceIsTapped() {
        Permanent ire = addReadyIre();
        ire.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(ire.isTapped()).isTrue();
    }

    @Test
    void canActivateRepeatedlyWithoutUntappingTheSource() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void canDeclineToTapAnUntappedCreature() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineToUntapACreatureTappedBeforeResolution() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());
        target.tap();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(target.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsControllersCreature() {
        addReadyIre();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PensiveMinotaur());
        target.tap();
        addIreMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyIre() {
        return harness.addToBattlefieldAndReturn(player1, new ThassasIre());
    }

    private void addIreMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
