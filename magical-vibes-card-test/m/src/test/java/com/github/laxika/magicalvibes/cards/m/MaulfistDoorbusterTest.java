package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TerrainElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaulfistDoorbuster.class, TerrainElemental.class})
class MaulfistDoorbusterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new MaulfistDoorbuster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy on attack to stop a creature from blocking")
    void paysEnergyOnAttack() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the payment leaves the target able to block")
    void declinesEnergyPayment() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot stop a creature from blocking without energy")
    void cannotPayWithoutEnergy() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Energy gained after attacking can pay for the attack trigger")
    void canPayWithEnergyGainedAfterAttacking() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger can target a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player1, new TerrainElemental());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An attack trigger with a departed target does not spend energy")
    void doesNotPayWhenTargetLeavesBattlefield() {
        addCreatureReady(player1, new MaulfistDoorbuster());
        Permanent target = addCreatureReady(player2, new TerrainElemental());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
