package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverbeingOfMyth.class, NettleSentinel.class})
class OverbeingOfMythTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent overbeing = addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel(), new NettleSentinel(), new NettleSentinel()));

        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent overbeing = addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.setHand(player2, List.of(new NettleSentinel(), new NettleSentinel()));

        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's draw step draws an additional card")
    void drawsExtraOnDrawStep() {
        addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel()));

        advanceToDraw(player1); // turn-based draw: 1 card
        harness.passBothPriorities(); // resolve Overbeing trigger: +1 card

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent's draw step does not draw an extra card for the controller")
    void doesNotDrawOnOpponentDrawStep() {
        addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel()));

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("P/T updates as cards leave the hand, and an empty hand causes death")
    void shrinkingHandUpdatesPowerToughnessAndCausesDeath() {
        Permanent overbeing = addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel(), new NettleSentinel()));
        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).removeFirst();
        assertThat(gqs.getEffectivePower(gd, overbeing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, overbeing)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(overbeing);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(overbeing.getCard());
    }

    @Test
    @DisplayName("Each copy triggers separately after the normal draw and grows as cards are drawn")
    void multipleCopiesDrawSeparatelyAndGrow() {
        Permanent first = addOverbeingReady(player1);
        Permanent second = addOverbeingReady(player1);
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel(), new NettleSentinel()));

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The characteristic ability also counts the owner's hand while in the graveyard")
    void characteristicAbilityWorksInGraveyard() {
        OverbeingOfMyth card = new OverbeingOfMyth();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new NettleSentinel(), new NettleSentinel()));
        harness.setHand(player2, List.of(new NettleSentinel()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private Permanent addOverbeingReady(Player player) {
        return addCreatureReady(player, new OverbeingOfMyth());
    }
}
