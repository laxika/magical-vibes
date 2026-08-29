package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelsTrumpetTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have vigilance")
    void grantsVigilanceToAllCreatures() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("At each end step, taps that player's untapped creatures that did not attack and deals equal damage")
    void tapsNonAttackingCreaturesAndDamagesActivePlayer() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        Permanent alreadyTapped = addCreatureReady(player1, new GrizzlyBears());
        alreadyTapped.tap();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(nonAttacker.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(alreadyTapped.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger affects the active player on an opponent's end step")
    void affectsOpponentAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
