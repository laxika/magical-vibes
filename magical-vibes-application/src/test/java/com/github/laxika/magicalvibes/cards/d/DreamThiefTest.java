package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlenElendraArchmage;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamThief.class, GlenElendraArchmage.class, NettleSentinel.class})
class DreamThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another blue spell was cast this turn")
    void drawsAfterAnotherBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel()));

        harness.setHand(player1, List.of(new GlenElendraArchmage(), new DreamThief()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0); // blue spell
        resolveAllTriggers();
        harness.castCreature(player1, 0); // Dream Thief
        resolveAllTriggers();

        // ETB drew a card; the one remaining library card is now in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Does not draw when it is the only spell cast this turn")
    void noDrawWhenNoOtherSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel()));

        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Does not draw when the only other spell cast was not blue")
    void noDrawAfterNonBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel()));

        harness.setHand(player1, List.of(new NettleSentinel(), new DreamThief()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0); // green spell
        resolveAllTriggers();
        harness.castCreature(player1, 0); // Dream Thief
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Does not draw for a blue spell cast by an opponent")
    void opponentBlueSpellDoesNotCount() {
        harness.setLibrary(player1, List.of(new NettleSentinel()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new GlenElendraArchmage(), "{3}{U}");
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Draws when another blue spell was cast even if Dream Thief was put onto the battlefield")
    void drawsWhenPutOntoBattlefieldAfterAnotherBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel()));

        harness.castFromHand(player1, new GlenElendraArchmage(), "{3}{U}");
        resolveAllTriggers();

        harness.enterBattlefieldAndReturn(player1, new DreamThief());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Dream Thief");
    }
}
