package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoroshTheHunter.class, GossamerPhantasm.class})
class VoroshTheHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{G} puts six +1/+1 counters on Vorosh")
    void payingManaPutsCountersOnVorosh() {
        Permanent vorosh = addAttackingVorosh();

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining the payment puts no counters on Vorosh")
    void decliningPaymentPutsNoCountersOnVorosh() {
        Permanent vorosh = addAttackingVorosh();

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blocked Vorosh does not trigger its combat-damage ability")
    void blockedVoroshDoesNotTrigger() {
        Permanent vorosh = addAttackingVorosh();
        Permanent blocker = addCreatureReady(player2, new GossamerPhantasm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(vorosh))));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Generic mana cannot replace the green mana in Vorosh's payment")
    void paymentRequiresGreenMana() {
        Permanent vorosh = addAttackingVorosh();
        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An incomplete payment puts no counters on Vorosh")
    void paymentRequiresAllThreeMana() {
        Permanent vorosh = addAttackingVorosh();
        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attacking Vorosh's controller pays and receives the counters")
    void opponentControlledVoroshReceivesCounters() {
        Permanent vorosh = addCreatureReady(player2, new VoroshTheHunter());
        vorosh.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent addAttackingVorosh() {
        Permanent vorosh = addCreatureReady(player1, new VoroshTheHunter());
        vorosh.setAttacking(true);
        return vorosh;
    }

    private void resolveCombatToMayPrompt() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
