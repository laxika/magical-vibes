package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenbeltRadical.class, GrizzlyBears.class})
class GreenbeltRadicalTest extends BaseCardTest {

    @Test
    void turningFaceUpPutsCountersOnOwnCreaturesAndGrantsTrampleUntilEndOfTurn() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GreenbeltRadical card = new GreenbeltRadical();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent radical = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(radical));
        harness.passBothPriorities();

        assertThat(radical.isFaceDown()).isFalse();
        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(radical.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, radical, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, radical, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent findPermanentForCard(GreenbeltRadical card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
