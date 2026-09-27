package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Teardrop Kami")
@CardUsed({TeardropKami.class, GnarledMass.class, TendoIceBridge.class})
class TeardropKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped target creature")
    void tapsUntappedCreature() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAbility(true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTappedCreature() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAbility(true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid at activation, before the ability resolves")
    void sacrificePaidAtActivation() {
        harness.addToBattlefield(player1, new TeardropKami());
        harness.addToBattlefield(player2, new GnarledMass());

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Gnarled Mass"));

        harness.assertNotOnBattlefield(player1, "Teardrop Kami");
        harness.assertInGraveyard(player1, "Teardrop Kami");
    }

    @Test
    @DisplayName("Declining the optional effect leaves the target unchanged")
    void decliningEffectLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAbility(false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveAbility(boolean accepted) {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accepted);
    }
}
