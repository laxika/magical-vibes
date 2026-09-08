package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TombRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with a land on top puts it into hand")
    void exploresLand() {
        Card land = new Forest();
        addTombRobber(land);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activateAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(findTombRobber().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activating with a nonland on top puts a +1/+1 counter on it")
    void exploresNonland() {
        Card nonland = new GrizzlyBears();
        addTombRobber(nonland);
        harness.setHand(player1, List.of(new Forest()));

        activateAndResolve();

        assertThat(findTombRobber().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addTombRobber(new Forest());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addTombRobber(Card topLibraryCard) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new TombRobber());
        harness.setLibrary(player1, List.of(topLibraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    private Permanent findTombRobber() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Tomb Robber"))
                .findFirst()
                .orElseThrow();
    }
}
