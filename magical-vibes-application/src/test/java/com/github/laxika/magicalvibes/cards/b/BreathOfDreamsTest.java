package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BreathOfDreamsTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying own cumulative upkeep keeps Breath of Dreams")
    void paysOwnCumulativeUpkeep() {
        Permanent breath = harness.addToBattlefieldAndReturn(player1, new BreathOfDreams());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(breath.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(breath);
    }

    @Test
    @DisplayName("Declining own cumulative upkeep sacrifices Breath of Dreams")
    void declineSacrificesBreath() {
        Permanent breath = harness.addToBattlefieldAndReturn(player1, new BreathOfDreams());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(breath);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Breath of Dreams"));
    }

    @Test
    @DisplayName("Green creatures get an age counter from granted cumulative upkeep")
    void greenCreatureGetsAgeCounterFromGrantedUpkeep() {
        Permanent breath = harness.addToBattlefieldAndReturn(player1, new BreathOfDreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        // Bears' granted CU resolves first (later on stack)
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(bears.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        // Breath's own CU
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, breath);
    }

    @Test
    @DisplayName("Paying granted cumulative upkeep keeps the green creature")
    void payingGrantedUpkeepKeepsGreenCreature() {
        Permanent breath = harness.addToBattlefieldAndReturn(player1, new BreathOfDreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        // Bears' granted CU resolves first
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        // Then Breath's own CU
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, breath);
        assertThat(bears.getCounterCount(CounterType.AGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grant is global: opponent's Breath still taxes your green creatures")
    void opponentsBreathTaxesYourGreenCreatures() {
        harness.addToBattlefield(player2, new BreathOfDreams());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(bears.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Non-green creatures are unaffected")
    void nonGreenUnaffected() {
        harness.addToBattlefield(player1, new BreathOfDreams());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        // Pay Breath's own CU
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merfolk);
        assertThat(merfolk.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
