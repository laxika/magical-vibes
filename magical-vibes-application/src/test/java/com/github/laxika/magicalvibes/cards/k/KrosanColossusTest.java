package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KrosanColossus.class)
class KrosanColossusTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new KrosanColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Krosan Colossus");
        assertThat(colossus.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int colossusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(colossus);
        harness.turnFaceUp(player1, colossusIndex);
        harness.passBothPriorities();

        assertThat(colossus.isFaceDown()).isFalse();
    }
}
