package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BondOfDiscipline.class, GrizzlyBears.class, FountainOfYouth.class})
class BondOfDisciplineTest extends BaseCardTest {

    @Test
    @DisplayName("Taps opposing creatures and grants lifelink to your creatures")
    void tapsOpposingCreaturesAndGrantsOwnLifelink() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingNonCreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingNonCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new BondOfDiscipline()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
