package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaltfieldRecluse.class, KavuPredator.class, UrborgTombOfYawgmoth.class})
class SaltfieldRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give target creature -2/-0 until end of turn")
    void givesTargetCreatureNegativePowerUntilEndOfTurn() {
        Permanent recluse = addCreatureReady(player1, new SaltfieldRecluse());
        Permanent kavu = addCreatureReady(player2, new KavuPredator());

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(recluse.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new SaltfieldRecluse());
        Permanent kavu = addCreatureReady(player1, new KavuPredator());

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new SaltfieldRecluse());
        Permanent kavu = addCreatureReady(player2, new KavuPredator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        assertThat(recluse.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent recluse = addCreatureReady(player1, new SaltfieldRecluse());
        recluse.tap();
        Permanent kavu = addCreatureReady(player2, new KavuPredator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent recluse = addCreatureReady(player1, new SaltfieldRecluse());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(recluse.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself and reduce its power below zero without changing toughness")
    void canTargetItself() {
        Permanent recluse = addCreatureReady(player1, new SaltfieldRecluse());

        harness.activateAbility(player1, 0, null, recluse.getId());
        harness.passBothPriorities();

        assertThat(recluse.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, recluse)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, recluse)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Saltfield Recluse");
    }

    @Test
    @DisplayName("Two activations cumulatively reduce the same creature's power")
    void multipleActivationsAreCumulative() {
        addCreatureReady(player1, new SaltfieldRecluse());
        addCreatureReady(player1, new SaltfieldRecluse());
        Permanent kavu = addCreatureReady(player2, new KavuPredator());

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, kavu.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Kavu Predator");
    }
}
