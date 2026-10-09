package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlenElendraArchmage;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.o.OonasGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamThief.class, GlenElendraArchmage.class, NettleSentinel.class, OonasGrace.class})
class DreamThiefTest extends BaseCardTest {

    @BeforeEach
    void clearHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Test
    @DisplayName("Draws a card when another blue spell was cast this turn")
    void drawsAfterAnotherBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel()));

        harness.castFromHand(player1, new GlenElendraArchmage(), "{3}{U}");
        resolveAllTriggers();
        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        resolveAllTriggers();

        // The ETB draws exactly one of the two library cards.
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

        harness.castFromHand(player1, new NettleSentinel(), "{G}");
        resolveAllTriggers();
        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
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

    @Test
    @DisplayName("Draws when the first other blue spell is cast in response to the ETB")
    void blueInstantInResponseEnablesDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel()));
        harness.setLibrary(player2, List.of(new NettleSentinel()));

        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dream Thief");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Another Dream Thief counts as another blue spell")
    void secondDreamThiefDraws() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new NettleSentinel(), new NettleSentinel()));

        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castFromHand(player1, new DreamThief(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Dream Thief")).isEqualTo(2);
    }

    @Test
    @DisplayName("Putting Dream Thief onto the battlefield is not casting a blue spell")
    void noDrawWhenPutOntoBattlefieldWithoutCasting() {
        harness.setLibrary(player1, List.of(new NettleSentinel()));

        harness.enterBattlefieldAndReturn(player1, new DreamThief());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }
}
