package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.m.MarshGas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagMan.class, BogImp.class, BogRats.class, MarshGas.class, BloodMoon.class})
class RagManTest extends BaseCardTest {

    private Permanent readyRagMan() {
        Permanent ragMan = addCreatureReady(player1, new RagMan());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return ragMan;
    }

    @Test
    @DisplayName("Discards the only creature card from target opponent's hand")
    void discardsCreatureAtRandom() {
        harness.setHand(player2, List.of(new MarshGas(), new BogRats(), new BloodMoon()));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only creature in hand — deterministically discarded, non-creatures untouched.
        harness.assertInGraveyard(player2, "Bog Rats");
        harness.assertNotInHand(player2, "Bog Rats");
        harness.assertInHand(player2, "Marsh Gas");
        harness.assertInHand(player2, "Blood Moon");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(gameLogContains("Marsh Gas")).isTrue();
        assertThat(gameLogContains("Blood Moon")).isTrue();
    }

    @Test
    @DisplayName("Only ever discards a creature card, never a noncreature")
    void onlyDiscardsCreatures() {
        harness.setHand(player2, List.of(new MarshGas(), new BogRats(), new BloodMoon(), new BogImp()));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Whichever creature is picked, the noncreature cards must all remain in hand.
        harness.assertInHand(player2, "Marsh Gas");
        harness.assertInHand(player2, "Blood Moon");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(1)
                .allMatch(c -> c.getName().equals("Bog Rats") || c.getName().equals("Bog Imp"));
    }

    @Test
    @DisplayName("Does nothing when the opponent has no creature cards")
    void noCreatureNoDiscard() {
        harness.setHand(player2, List.of(new MarshGas(), new BloodMoon()));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles an empty hand gracefully")
    void emptyHandNoDiscard() {
        harness.setHand(player2, List.of());
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, List.of(new BogRats()));
        addCreatureReady(player1, new RagMan());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        readyRagMan();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays three black mana and taps Rag Man to activate")
    void paysActivationCostAndTapsRagMan() {
        harness.setHand(player2, List.of(new BogRats()));
        Permanent ragMan = readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(ragMan.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();
    }
}
