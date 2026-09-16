package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Demoralize.class, DuskImp.class})
class DemoralizeTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain menace until end of turn")
    void allCreaturesGainMenace() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());

        castDemoralize();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.MENACE);
        assertThat(opposingCreature.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Threshold prevents all creatures from blocking")
    void thresholdPreventsAllCreaturesFromBlocking() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opposingCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A creature entering after threshold resolves still can't block this turn")
    void creatureEnteringAfterThresholdResolvesCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Fewer than seven cards in the caster's graveyard do not enable threshold")
    void thresholdDoesNotApplyBelowSevenCards() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()
        ));

        castDemoralize();

        assertThat(opposingCreature.isCantBlockThisTurn()).isFalse();
    }

    private void castDemoralize() {
        harness.castFromHand(player1, new Demoralize(), "{2}{R}");
        harness.passBothPriorities();
    }
}
