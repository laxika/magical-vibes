package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.t.ThrivingRhino;
import com.github.laxika.magicalvibes.cards.u.UnderhandedDesigns;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JanjeetSentry.class, ThrivingRhino.class, PropheticPrism.class, UnderhandedDesigns.class})
class JanjeetSentryTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new JanjeetSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEnergyAndTapsTargetCreature() {
        addReadySentry(player1);
        Permanent target = addCreatureReady(player2, new ThrivingRhino());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untapsTargetArtifact() {
        addReadySentry(player1);
        Permanent target = addReadyArtifact(player2);
        target.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addReadySentry(player1);
        Permanent target = addCreatureReady(player2, new ThrivingRhino());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }

    @Test
    void cannotTargetAnEnchantment() {
        addReadySentry(player1);
        Permanent target = addReadyEnchantment(player2);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void mayDeclineTappingOrUntappingTarget() {
        addReadySentry(player1);
        Permanent target = addCreatureReady(player2, new ThrivingRhino());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void costsArePaidBeforeResolutionEvenWhenActionIsDeclined() {
        Permanent sentry = addReadySentry(player1);
        Permanent target = addReadyArtifact(player2);
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(sentry.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentry.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfAndUntapAfterPayingTapCost() {
        Permanent sentry = addReadySentry(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, sentry.getId());
        assertThat(sentry.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentry.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent sentry = addReadySentry(player1);
        sentry.setSummoningSick(true);
        Permanent target = addReadyArtifact(player2);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(sentry.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent sentry = addReadySentry(player1);
        sentry.tap();
        Permanent target = addReadyArtifact(player2);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void untapsTargetCreature() {
        addReadySentry(player1);
        Permanent target = addCreatureReady(player2, new ThrivingRhino());
        target.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tapsTargetArtifactControlledByItsController() {
        addReadySentry(player1);
        Permanent target = addReadyArtifact(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void targetLeavingBattlefieldDoesNotRefundCosts() {
        Permanent sentry = addReadySentry(player1);
        Permanent target = addReadyArtifact(player2);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(sentry.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadySentry(Player player) {
        return addCreatureReady(player, new JanjeetSentry());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PropheticPrism());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new UnderhandedDesigns());
    }
}
