package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BolracClanBasher.class)
class BolracClanBasherTest extends BaseCardTest {

    @Test
    void disguiseCastsAndTurnsBolracClanBasherFaceUp() {
        harness.setHand(player1, List.of(new BolracClanBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent basher = findPermanent(player1, "Bolrac-Clan Basher");
        assertThat(basher.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(basher));

        assertThat(basher.isFaceDown()).isFalse();
    }
}
