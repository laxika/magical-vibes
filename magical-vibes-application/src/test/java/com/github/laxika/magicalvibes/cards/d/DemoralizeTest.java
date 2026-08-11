package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoralizeTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain menace until end of turn")
    void allCreaturesGainMenace() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDemoralize();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(opposingCreature.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Threshold prevents all creatures from blocking")
    void thresholdPreventsAllCreaturesFromBlocking() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        castDemoralize();

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opposingCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Fewer than seven cards in the caster's graveyard do not enable threshold")
    void thresholdDoesNotApplyBelowSevenCards() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        castDemoralize();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        castDemoralize();

        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    private void castDemoralize() {
        harness.setHand(player1, List.of(new Demoralize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
