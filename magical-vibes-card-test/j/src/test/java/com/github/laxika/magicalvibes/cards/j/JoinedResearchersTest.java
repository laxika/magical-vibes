package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoinedResearchers.class, Island.class})
class JoinedResearchersTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared at each end step when an opponent has more cards in hand")
    void becomesPreparedWhenOpponentHasMoreCardsInHand() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(researchers.isPrepared()).isTrue();
        assertThat(researchers.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when no opponent has more cards in hand")
    void doesNotBecomePreparedWithoutHandSizeDifference() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Island()));

        advanceToEndStep(player2);

        assertThat(researchers.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Secret Rendezvous makes each player draw three cards and unprepares the creature")
    void castingPreparedSpellDrawsForBothPlayers() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        UUID spellId = researchers.getPreparedSpellCardId();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spellId, player2.getId());
        assertThat(researchers.isPrepared()).isFalse();
        assertThat(researchers.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(researchers.isPrepared()).isFalse();
        assertThat(researchers.getPreparedSpellCardId()).isNull();
    }

    @Test
    void becomesPreparedOnControllersEndStep() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(researchers.isPrepared()).isTrue();
    }

    @Test
    void doesNotPrepareIfHandSizesBecomeEqualBeforeResolution() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Island()));
        harness.passBothPriorities();

        assertThat(researchers.isPrepared()).isFalse();
        assertThat(researchers.getPreparedSpellCardId()).isNull();
    }

    @Test
    void gainingHandSizeAdvantageAfterEndStepBeginsDoesNotTrigger() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceToEndStep(player2);

        harness.setHand(player2, List.of(new Island()));

        assertThat(gd.stack).isEmpty();
        assertThat(researchers.isPrepared()).isFalse();
    }

    @Test
    void becomingPreparedAgainKeepsTheSameSpellCopy() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        advanceToEndStep(player2);
        harness.passBothPriorities();
        UUID spellId = researchers.getPreparedSpellCardId();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(researchers.isPrepared()).isTrue();
        assertThat(researchers.getPreparedSpellCardId()).isEqualTo(spellId);
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    void preparedSpellCannotTargetItsController() {
        Permanent researchers = harness.addToBattlefieldAndReturn(player1, new JoinedResearchers());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        advanceToEndStep(player2);
        harness.passBothPriorities();
        UUID spellId = researchers.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, spellId, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(researchers.isPrepared()).isTrue();
        assertThat(researchers.getPreparedSpellCardId()).isEqualTo(spellId);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
