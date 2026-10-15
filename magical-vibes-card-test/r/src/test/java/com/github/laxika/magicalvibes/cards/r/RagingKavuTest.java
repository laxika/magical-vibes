package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagingKavu.class})
class RagingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Flash lets Raging Kavu be cast during an opponent's turn and haste lets it attack immediately")
    void flashesInAndAttacksImmediately() {
        harness.addToBattlefield(player2, new RagingKavu());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RagingKavu(), "{1}{R}{G}");
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Raging Kavu");

        harness.forceActivePlayer(player1);
        declareAttackers(player1, List.of(0));

        assertThat(kavu.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flash allows Raging Kavu to resolve above another creature spell")
    void castsInResponseToCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RagingKavu(), "{1}{R}{G}");
        harness.castFromHand(player1, new RagingKavu(), "{1}{R}{G}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Kavu");
        assertThat(countPermanents(player2, "Raging Kavu")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Raging Kavu");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Raging Kavu cast at the beginning of combat can attack that turn")
    void castsBeforeAttackersAndAttacksImmediately() {
        harness.addToBattlefield(player2, new RagingKavu());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.castFromHand(player1, new RagingKavu(), "{1}{R}{G}");
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Raging Kavu");
        assertThat(kavu.isSummoningSick()).isTrue();

        declareAttackers(player1, List.of(0));

        assertThat(kavu.isAttacking()).isTrue();
    }
}
