package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gastal Thrillroller")
class GastalThrillrollerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB animates it until end of turn")
    void etbAnimatesUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GastalThrillroller()));
        addThrillrollerMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thrillroller = findPermanent(player1, "Gastal Thrillroller");
        assertThat(gqs.isCreature(gd, thrillroller)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thrillroller)).isFalse();
    }

    @Test
    @DisplayName("Crew 2 animates it and taps the crew")
    void crewAnimatesAndTapsCrew() {
        Permanent thrillroller = addThrillrollerReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thrillroller)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns from the graveyard after discarding and gains a finality counter")
    void returnsFromGraveyardWithFinalityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GastalThrillroller card = new GastalThrillroller();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest()));
        addThrillrollerMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thrillroller = findPermanent(player1, "Gastal Thrillroller");
        assertThat(thrillroller.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A finality counter exiles it instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent thrillroller = addThrillrollerReady(player1);
        thrillroller.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, thrillroller));

        harness.assertNotOnBattlefield(player1, "Gastal Thrillroller");
        harness.assertNotInGraveyard(player1, "Gastal Thrillroller");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gastal Thrillroller"));
    }

    private void addThrillrollerMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addThrillrollerReady(Player player) {
        Permanent permanent = new Permanent(new GastalThrillroller());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
