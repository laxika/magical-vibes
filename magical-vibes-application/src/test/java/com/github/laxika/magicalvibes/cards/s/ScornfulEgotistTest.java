package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScornfulEgotist.class)
class ScornfulEgotistTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        int egotistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(egotist);
        harness.turnFaceUp(player1, egotistIndex);
        harness.passBothPriorities();

        assertThat(egotist.isFaceDown()).isFalse();
    }
}
