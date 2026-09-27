package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsperSentinel.class, GrizzlyBears.class, HonorOfThePure.class, MindStone.class})
class EsperSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers on an opponent's first noncreature spell each turn")
    void triggersOnFirstOpponentNoncreatureSpell() {
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new GrizzlyBears(), new MindStone(), new MindStone()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Uses Esper Sentinel's power as the payment amount")
    void usesPowerAsPaymentAmount() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Offers the draw when the opponent cannot pay Sentinel's power")
    void drawsWhenOpponentCannotPayPower() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
