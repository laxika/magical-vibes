package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrohTeaMaster.class, GrizzlyBears.class})
class IrohTeaMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenWhenItEnters() {
        harness.setHand(player1, List.of(new IrohTeaMaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Giving away a permanent creates an Ally with counters for owned permanents")
    void givingAwayPermanentCreatesAllyWithCounters() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(donated);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(donated);
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ability does not give away a permanent or create an Ally")
    void decliningAbilityDoesNothing() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(donated);
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
