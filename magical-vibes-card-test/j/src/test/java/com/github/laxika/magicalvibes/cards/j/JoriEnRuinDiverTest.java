package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RealityHemorrhage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoriEnRuinDiver.class, RealityHemorrhage.class})
class JoriEnRuinDiverTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn draws a card")
    void secondSpellEachTurnDrawsCard() {
        harness.addToBattlefield(player1, new JoriEnRuinDiver());
        harness.setLibrary(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage(), new RealityHemorrhage()));
        harness.setHand(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage(), new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A spell cast by an opponent does not trigger Jori En")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JoriEnRuinDiver());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RealityHemorrhage()));
        harness.addMana(player2, ManaColor.RED, 2);

        int player1HandSize = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
    }

    @Test
    void castingJoriEnAsFirstSpellCountsTowardSecondSpell() {
        harness.setHand(player1, List.of(new JoriEnRuinDiver(), new RealityHemorrhage()));
        harness.setLibrary(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSecondSpellDoesNotDrawForEitherPlayer() {
        harness.addToBattlefield(player1, new JoriEnRuinDiver());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RealityHemorrhage()));
        harness.setHand(player2, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.setLibrary(player2, List.of(new RealityHemorrhage()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void castingJoriEnAsSecondSpellDoesNotTriggerEvenOnThirdSpell() {
        harness.setHand(player1, List.of(new RealityHemorrhage(), new JoriEnRuinDiver(), new RealityHemorrhage()));
        harness.setLibrary(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void secondSpellTriggersWithFirstSpellStillOnStack() {
        harness.addToBattlefield(player1, new JoriEnRuinDiver());
        harness.setHand(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.setLibrary(player1, List.of(new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    void spellCountResetsAndControllerCanTriggerDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new JoriEnRuinDiver());
        harness.setHand(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.setLibrary(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.setLibrary(player2, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RealityHemorrhage(), new RealityHemorrhage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
    }
}
