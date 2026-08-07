package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaveOfTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Draw step destroys creatures whose mana value equals the age counter count")
    void destroysCreaturesWithMatchingManaValue() {
        Permanent wave = addWave(player1);
        wave.setCounterCount(CounterType.AGE, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent lions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears).contains(lions);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The sweep hits the controller's own creatures too")
    void destroysOwnCreatures() {
        Permanent wave = addWave(player1);
        wave.setCounterCount(CounterType.AGE, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A single age counter sweeps mana value 1, sparing 0-cost creatures")
    void oneAgeCounterSweepsManaValueOne() {
        Permanent wave = addWave(player1);
        wave.setCounterCount(CounterType.AGE, 1);
        Permanent lions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lions).contains(thopter);
    }

    @Test
    @DisplayName("The age counter added during upkeep is already counted at the draw step")
    void upkeepAgeCounterCountsForTheSameTurnSweep() {
        addWave(player1);
        Permanent lions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP queues cumulative upkeep
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities(); // cumulative upkeep resolves
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // UPKEEP -> DRAW fires the sweep
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lions);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Wave of Terror")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent wave = addWave(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wave);
        harness.assertInGraveyard(player1, "Wave of Terror");
    }

    private Permanent addWave(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new WaveOfTerror());
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW fires the draw-step trigger
    }
}
