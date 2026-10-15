package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleavingReaper.class, Forest.class})
class CleavingReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself from the graveyard to hand after an Angel or Berserker enters")
    void returnsToHandAfterMatchingCreatureEnters() {
        Card reaper = new CleavingReaper();
        harness.setGraveyard(player1, List.of(reaper));
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new CleavingReaper());

        harness.activateGraveyardAbility(player1, 0);

        harness.assertLife(player1, 17);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cleaving Reaper");
        harness.assertNotInGraveyard(player1, "Cleaving Reaper");
    }

    @Test
    @DisplayName("Does not activate without a matching creature entering under your control")
    void cannotActivateWithoutMatchingCreatureEntering() {
        Card reaper = new CleavingReaper();
        harness.setGraveyard(player1, List.of(reaper));
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cleaving Reaper");
    }

    @Test
    @DisplayName("Does not count an Angel or Berserker entering under an opponent's control")
    void cannotActivateFromOpponentCreatureEntering() {
        Card reaper = new CleavingReaper();
        harness.setGraveyard(player1, List.of(reaper));
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player2, new CleavingReaper());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cleaving Reaper");
    }
}
