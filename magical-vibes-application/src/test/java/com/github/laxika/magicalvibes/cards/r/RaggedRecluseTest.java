package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OdiousWitch;
import com.github.laxika.magicalvibes.cards.t.ThirstForDiscovery;
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

@CardUsed({RaggedRecluse.class, OdiousWitch.class, ThirstForDiscovery.class,
        Forest.class, Island.class, Mountain.class})
class RaggedRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at the end of a turn in which its controller discarded a card")
    void transformsAfterControllerDiscards() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());
        setUpDiscardSpell();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(recluse.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at the end of a turn in which its controller did not discard")
    void doesNotTransformWithoutControllerDiscard() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());

        advanceToEndStep();

        assertThat(recluse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("An opponent's discard does not cause the controller's end-step transformation")
    void doesNotTransformAfterOnlyOpponentDiscards() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());
        setUpDiscardSpell(player2);

        harness.castAndResolveInstant(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, -1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(recluse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Discarding after the end step begins does not retroactively trigger transformation")
    void doesNotTransformAfterLateDiscard() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());
        advanceToEndStep();
        setUpDiscardSpell();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(recluse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("A discard during the opponent's turn does not transform Ragged Recluse at their end step")
    void doesNotTransformDuringOpponentsEndStep() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());
        setUpDiscardSpell();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(recluse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Odious Witch drains the defending player when it attacks")
    void backFaceDrainsDefendingPlayerWhenAttacking() {
        RaggedRecluse card = new RaggedRecluse();
        Permanent witch = addCreatureReady(player1, card);
        witch.setCard(card.getBackFaceCard());
        witch.setTransformed(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 16);
    }

    private void setUpDiscardSpell() {
        setUpDiscardSpell(player1);
    }

    private void setUpDiscardSpell(Player player) {
        harness.setLibrary(player, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player, List.of(new ThirstForDiscovery(), new RaggedRecluse(), new Forest()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
