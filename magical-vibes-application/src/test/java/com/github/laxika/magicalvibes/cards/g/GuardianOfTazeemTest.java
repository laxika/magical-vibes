package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfTazeem.class, Forest.class, Island.class, GrizzlyBears.class})
class GuardianOfTazeemTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall taps an opponent's creature")
    void landfallTapsOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        playLand(new Forest());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Island landfall makes the creature skip its next untap step")
    void islandLandfallSkipsNextUntap() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        playLand(new Island());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    private void playLand(Card land) {
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }
}
