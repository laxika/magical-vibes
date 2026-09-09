package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicChampion.class, Censor.class, GrizzlyBears.class})
class StoicChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives Stoic Champion +2/+2")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new StoicChampion());
        setUpCycling(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(champion.getPowerModifier()).isEqualTo(2);
        assertThat(champion.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent cycling a card gives Stoic Champion +2/+2")
    void opponentsCyclingBoostsSelf() {
        harness.addToBattlefield(player1, new StoicChampion());
        setUpCycling(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(champion.getPowerModifier()).isEqualTo(2);
        assertThat(champion.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple cycles stack and the boost wears off at end of turn")
    void cyclesStackUntilEndOfTurn() {
        harness.addToBattlefield(player1, new StoicChampion());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Stoic Champion");
        assertThat(champion.getPowerModifier()).isEqualTo(4);
        assertThat(champion.getToughnessModifier()).isEqualTo(4);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(0);
        assertThat(champion.getToughnessModifier()).isEqualTo(0);
    }

    private void setUpCycling(Player player) {
        harness.setHand(player, List.of(new Censor()));
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
