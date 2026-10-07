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

    @Test
    @DisplayName("Can sacrifice a tapped, summoning-sick Kami to untap your own creature")
    void tappedSummoningSickSourceCanActivate() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        kami.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAbility(true);

        assertThat(target.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Teardrop Kami");
        harness.assertInGraveyard(player1, "Teardrop Kami");
    }

    @Test
    @DisplayName("Declining leaves a tapped target tapped and does not refund the sacrifice")
    void decliningEffectLeavesTargetTapped() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAbility(false);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Teardrop Kami");
        harness.assertNotOnBattlefield(player1, "Teardrop Kami");
    }

    @Test
    @DisplayName("Uses the target's tapped state at resolution")
    void targetTappedInResponseIsUntapped() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.addToBattlefield(player2, new TeardropKami());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(target.isTapped()).isTrue();

        resolveAbility(true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed target makes the ability fail to resolve")
    void canTargetItself() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new TeardropKami());

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teardrop Kami");
        harness.assertNotOnBattlefield(player1, "Teardrop Kami");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not offer the optional effect when the target was sacrificed in response")
    void targetSacrificedInResponse() {
        harness.addToBattlefield(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TeardropKami());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GnarledMass());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, other.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teardrop Kami");
        harness.assertInGraveyard(player2, "Teardrop Kami");
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveAbility(boolean accepted) {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accepted);
    }
}
