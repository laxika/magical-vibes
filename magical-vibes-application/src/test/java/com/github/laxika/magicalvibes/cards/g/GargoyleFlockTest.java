package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GargoyleFlock.class, GrizzlyBears.class})
class GargoyleFlockTest extends BaseCardTest {

    @Test
    void createsFlyingBlueTyranidGargoyleWhenCreatureEnteredThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveControllerEndStep();

        Permanent token = findPermanent(player1, "Tyranid Gargoyle");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.GARGOYLE);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotCreateTokenWithoutCreatureEnteringThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
    }

    @Test
    void doesNotCountOpponentCreatureEnteringThisTurn() {
        harness.addToBattlefield(player1, new GargoyleFlock());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Tyranid Gargoyle")).isZero();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
