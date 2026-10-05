package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageIntoTheValley.class, GrizzlyBears.class})
class RageIntoTheValleyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, loses 1 life, and amasses Goblins 2 without an Army")
    void drawsLosesLifeAndCreatesGoblinArmy() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new RageIntoTheValley()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 19);
        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds two +1/+1 counters and Goblin to an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new RageIntoTheValley()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Chooses one of multiple Armies and preserves its existing counters and types")
    void choosesOnlyOneArmy() {
        Permanent firstArmy = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent chosenArmy = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosenArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        chosenArmy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        RageIntoTheValley drawnCard = new RageIntoTheValley();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new RageIntoTheValley()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenArmy.getId()));

        assertThat(chosenArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(chosenArmy.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
        assertThat(gqs.hasEffectiveSubtype(gd, chosenArmy, CardSubtype.BEAR)).isTrue();
        assertThat(firstArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(firstArmy, chosenArmy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Army does not prevent creating your own Goblin Army")
    void ignoresOpponentsArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setLibrary(player1, List.of(new RageIntoTheValley()));
        harness.setHand(player1, List.of(new RageIntoTheValley()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
