package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousBrutalizer.class})
class VenomousBrutalizerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} after entering proliferates")
    void payingManaProliferates() {
        Permanent creature = addCounteredCreature();
        castVenomousBrutalizer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the entry payment does not proliferate")
    void decliningManaDoesNotProliferate() {
        Permanent creature = addCounteredCreature();
        castVenomousBrutalizer();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Toxic 3 gives three poison counters alongside combat damage without using the stack")
    void combatDamageGivesThreePoisonCounters() {
        Permanent attacker = addCreatureReady(player1, new VenomousBrutalizer());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Proliferation adds every existing counter kind to chosen permanents and players only")
    void proliferatesAllCounterKindsOnChosenObjects() {
        Permanent creature = addCounteredCreature();
        creature.setCounterCount(CounterType.CHARGE, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        castVenomousBrutalizer();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Payment is spent even when the player chooses nothing to proliferate")
    void mayChooseNothingAfterPaying() {
        Permanent creature = addCounteredCreature();
        castVenomousBrutalizer();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the green portion of the entry payment")
    void cannotProliferateWithoutGreenMana() {
        Permanent creature = addCounteredCreature();
        castVenomousBrutalizer();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying with no counters anywhere completes without a selection prompt")
    void payingWithNoCountersCompletes() {
        castVenomousBrutalizer();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCounteredCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VenomousBrutalizer());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return creature;
    }

    private void castVenomousBrutalizer() {
        harness.setHand(player1, List.of(new VenomousBrutalizer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
