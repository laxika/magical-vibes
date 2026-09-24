package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrastaTempestsRoar.class, LightningBolt.class})
class ThrastaTempestsRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {3} less for each other spell cast by any player this turn")
    void costReductionCountsSpellsCastByAnyPlayer() {
        harness.setHand(player1, List.of(new LightningBolt(), new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player1);
        resolveAllStack();

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Has hexproof until the end of the turn it enters")
    void hexproofExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thrasta = findPermanent(player1, "Thrasta, Tempest's Roar");
        assertThat(gqs.hasKeyword(gd, thrasta, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thrasta, Keyword.HEXPROOF)).isFalse();
    }

    private void resolveAllStack() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
