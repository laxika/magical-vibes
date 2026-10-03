package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconHawk.class, AzoriusSignet.class})
class BeaconHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may untap a target creature")
    void mayUntapTargetCreatureOnCombatDamage() {
        attackWithBeaconHawk(player1);
        Permanent target = addCreatureReady(player2, new BeaconHawk());
        target.tap();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the combat trigger does not untap the target creature")
    void decliningCombatTriggerDoesNothing() {
        attackWithBeaconHawk(player1);
        Permanent target = addCreatureReady(player2, new BeaconHawk());
        target.tap();

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The combat trigger cannot choose a noncreature permanent")
    void cannotChooseNonCreaturePermanent() {
        Permanent hawk = attackWithBeaconHawk(player1);
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        resolveCombatAndTrigger();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The activated ability gives Beacon Hawk +0/+1 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent hawk = addCreatureReady(player1, new BeaconHawk());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hawk.getPowerModifier()).isEqualTo(0);
        assertThat(hawk.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hawk.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Beacon Hawk can untap itself after dealing combat damage")
    void canUntapItself() {
        Permanent hawk = attackWithBeaconHawk(player1);
        hawk.tap();

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hawk.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Repeated activations each add toughness only to their source")
    void repeatedActivationsStackOnSourceOnly() {
        Permanent hawk = addCreatureReady(player1, new BeaconHawk());
        Permanent other = addCreatureReady(player1, new BeaconHawk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(hawk.getPowerModifier()).isZero();
        assertThat(hawk.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped summoning-sick Beacon Hawk can activate its toughness ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new BeaconHawk());
        hawk.setSummoningSick(true);
        hawk.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hawk.getPowerModifier()).isZero();
        assertThat(hawk.getToughnessModifier()).isEqualTo(1);
        assertThat(hawk.isTapped()).isTrue();
    }

    private Permanent attackWithBeaconHawk(Player player) {
        Permanent hawk = addCreatureReady(player, new BeaconHawk());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());
        return hawk;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat(player1);
        resolveAllTriggers();
    }
}
