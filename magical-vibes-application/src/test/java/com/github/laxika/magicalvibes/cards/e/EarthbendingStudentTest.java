package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthbendingStudent.class, Forest.class, GrizzlyBears.class})
class EarthbendingStudentTest extends BaseCardTest {

    @Test
    void entersByEarthbendingALandAndGivesLandCreaturesVigilance() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new EarthbendingStudent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ordinaryCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLand, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void cannotEarthbendAnOpponentsLand() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthbendingStudent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
