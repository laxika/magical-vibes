package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TitanicBulvox.class)
class TitanicBulvoxTest extends BaseCardTest {

    @Test
    void hasTrampleOnTheBattlefield() {
        Permanent bulvox = harness.addToBattlefieldAndReturn(player1, new TitanicBulvox());

        assertThat(gqs.hasKeyword(gd, bulvox, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new TitanicBulvox()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bulvox = findPermanent(player1, "Titanic Bulvox");
        assertThat(bulvox.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bulvox));
        harness.passBothPriorities();

        assertThat(bulvox.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, bulvox, Keyword.TRAMPLE)).isTrue();
    }
}
