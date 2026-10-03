package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RedHerring;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliminateTheImpossible.class, RedHerring.class})
class EliminateTheImpossibleTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates, weakens opponent creatures, and clears their suspected designation")
    void investigatesWeakensAndUnsuspectsOpponentCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        own.setSuspected(true);
        opponent.setSuspected(true);

        castEliminateTheImpossible();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(0);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(own.isSuspected()).isTrue();
        assertThat(opponent.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void powerReductionWearsOffAtEndOfTurn() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RedHerring());

        castEliminateTheImpossible();
        assertThat(opponent.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Investigates without any creatures and creates a usable Clue")
    void investigatesOnEmptyBattlefieldAndClueDrawsCard() {
        EliminateTheImpossible drawnCard = new EliminateTheImpossible();
        harness.setLibrary(player1, List.of(drawnCard));

        castEliminateTheImpossible();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Affects every current opposing creature but leaves later arrivals unchanged")
    void affectsOnlyCreaturesPresentAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        first.setSuspected(true);
        second.setSuspected(true);

        castEliminateTheImpossible();

        Permanent later = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        later.setSuspected(true);

        assertThat(first.getEffectivePower()).isZero();
        assertThat(second.getEffectivePower()).isZero();
        assertThat(first.isSuspected()).isFalse();
        assertThat(second.isSuspected()).isFalse();
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.isSuspected()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(first.isSuspected()).isFalse();
        assertThat(second.isSuspected()).isFalse();
        assertThat(later.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Repeated casts stack the power reduction without reducing toughness")
    void repeatedCastsCanReducePowerBelowZero() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RedHerring());

        castEliminateTheImpossible();
        castEliminateTheImpossible();

        assertThat(opponent.getEffectivePower()).isEqualTo(-2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    private void castEliminateTheImpossible() {
        harness.castFromHand(player1, new EliminateTheImpossible(), "{1}{U}");
        harness.passBothPriorities();
    }
}
