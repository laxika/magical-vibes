package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoyagingSatyr.class, Forest.class, GrizzlyBears.class})
class VoyagingSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target land")
    void untapsTargetLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Voyaging Satyr").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's land")
    void untapsOpponentLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new VoyagingSatyr());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapping a land uses the stack and affects only the chosen land")
    void untapsOnlyChosenLandOnResolution() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();
        other.tap();

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(satyr.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped land is a legal target")
    void canTargetUntappedLand() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(satyr.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new VoyagingSatyr());
        satyr.setSummoningSick(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(satyr.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Satyr cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        satyr.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
