package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsulsShieldguard.class, KujarSeedsculptor.class})
class ConsulsShieldguardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.castFromHand(player1, new ConsulsShieldguard(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy to give another attacking creature indestructible")
    void paysEnergyToGrantIndestructible() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent seedsculptor = addCreatureReady(player1, new KujarSeedsculptor());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, seedsculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot give indestructible without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent seedsculptor = addCreatureReady(player1, new KujarSeedsculptor());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, seedsculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Consul's Shieldguard itself")
    void cannotTargetItself() {
        Permanent shieldguard = addCreatureReady(player1, new ConsulsShieldguard());
        addCreatureReady(player1, new KujarSeedsculptor());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, shieldguard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not ask for a target when attacking alone")
    void noTargetChoiceWhenAttackingAlone() {
        addCreatureReady(player1, new ConsulsShieldguard());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining payment preserves energy and grants no indestructible")
    void decliningPaymentPreservesEnergy() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent seedsculptor = addCreatureReady(player1, new KujarSeedsculptor());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, seedsculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent attacker = addCreatureReady(player1, new KujarSeedsculptor());
        Permanent nonAttacker = addCreatureReady(player1, new KujarSeedsculptor());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An illegal target at resolution prevents the energy payment")
    void illegalTargetPreventsPayment() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent seedsculptor = addCreatureReady(player1, new KujarSeedsculptor());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, seedsculptor.getId());
        seedsculptor.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Indestructible expires at end of turn and payment removes only one energy")
    void indestructibleExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ConsulsShieldguard());
        Permanent seedsculptor = addCreatureReady(player1, new KujarSeedsculptor());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, seedsculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(seedsculptor.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }
}
