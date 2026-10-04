package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glademuse.class, Shock.class, LlanowarElves.class})
class GlademuseTest extends BaseCardTest {

    @Test
    @DisplayName("A player casting during another player's turn draws a card")
    void nonActiveCasterDrawsACard() {
        harness.addToBattlefield(player1, new Glademuse());
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new LlanowarElves()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player casting during their own turn does not draw a card")
    void activeCasterDoesNotDrawACard() {
        harness.addToBattlefield(player1, new Glademuse());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Glademuse's controller also draws when casting during an opponent's turn")
    void controllerCastingOffTurnDrawsBeforeSpellResolves() {
        harness.addToBattlefield(player1, new Glademuse());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Glademuse draws a card for the caster, regardless of its controller")
    void multipleGlademusesEachDrawForCaster() {
        harness.addToBattlefield(player1, new Glademuse());
        harness.addToBattlefield(player2, new Glademuse());
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new LlanowarElves(), new LlanowarElves()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Glademuse does not trigger its own ability")
    void glademuseDoesNotTriggerFromItsOwnCast() {
        harness.setLibrary(player1, List.of(new Glademuse()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Glademuse(), "{2}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glademuse");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
