package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Insurrection.class, ElvishWarrior.class, Mountain.class})
class InsurrectionTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all creatures, gains control of them, and grants haste")
    void untapsStealsAndGrantsHaste() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        opponentCreature.tap();
        ownCreature.tap();
        opponentLand.tap();

        castInsurrection();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand);
        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not affect creatures that enter after resolution")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castInsurrection();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(laterCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laterCreature);
        assertThat(laterCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Temporary control and haste expire at end of turn")
    void controlAndHasteExpireAtEndOfTurn() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castInsurrection();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void castInsurrection() {
        harness.castFromHand(player1, new Insurrection(), "{5}{R}{R}{R}");
        harness.passBothPriorities();
    }
}
