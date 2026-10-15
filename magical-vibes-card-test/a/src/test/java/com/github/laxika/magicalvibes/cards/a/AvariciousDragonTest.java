package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({AvariciousDragon.class, ElvishVisionary.class, TurnToFrog.class})
class AvariciousDragonTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(TurnStep.DRAW);
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Draw step draws an additional card")
    void drawStepDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player1, List.of());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve draw trigger

        // Normal draw (1) + additional draw (1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Extra card only on controller's draw step, not opponent's")
    void extraCardOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player2, List.of());

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("End step discards the controller's entire hand")
    void endStepDiscardsHand() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player1, List.of(new ElvishVisionary(), new ElvishVisionary()));

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Elvish Visionary"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Discard only on controller's end step, not opponent's")
    void discardOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player2, List.of(new ElvishVisionary()));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ability removal before the draw step prevents the additional draw")
    void noAdditionalDrawAfterLosingAbilities() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AvariciousDragon());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, dragon.getId());

        harness.passUntil(TurnStep.DRAW);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing the source does not stop an already triggered discard")
    void discardResolvesAfterSourceLeaves() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AvariciousDragon());
        ElvishVisionary originalCard = new ElvishVisionary();
        ElvishVisionary laterCard = new ElvishVisionary();
        harness.setHand(player1, List.of(originalCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalCard);
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerHands.get(player1.getId()).add(laterCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(originalCard, laterCard);
    }

    @Test
    @DisplayName("Each Dragon provides a separate additional draw")
    void multipleDragonsDrawAdditionalCards() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player1, List.of());

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Discarding an empty hand resolves without a choice")
    void discardsEmptyHand() {
        harness.addToBattlefield(player1, new AvariciousDragon());
        harness.setHand(player1, List.of());

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
