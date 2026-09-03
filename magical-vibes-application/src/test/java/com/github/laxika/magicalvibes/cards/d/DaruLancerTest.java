package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruLancer.class})
class DaruLancerTest extends BaseCardTest {

    @Test
    void hasFirstStrikeOnTheBattlefield() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new DaruLancer());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Daru Lancer");
        assertThat(lancer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        int lancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lancer);
        harness.turnFaceUp(player1, lancerIndex);
        harness.passBothPriorities();

        assertThat(lancer.isFaceDown()).isFalse();
    }
}
