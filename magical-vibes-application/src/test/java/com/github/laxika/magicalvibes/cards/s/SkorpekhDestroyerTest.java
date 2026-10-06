package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkorpekhDestroyer.class, Ornithopter.class, GrizzlyBears.class, FountainOfYouth.class})
class SkorpekhDestroyerTest extends BaseCardTest {

    @Test
    void gainsFirstStrikeWhenAnArtifactYouControlEnters() {
        Permanent destroyer = harness.addToBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void opponentArtifactDoesNotTriggerIt() {
        Permanent destroyer = harness.addToBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void nonartifactDoesNotTriggerIt() {
        Permanent destroyer = harness.addToBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent destroyer = harness.addToBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.passBothPriorities();
        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void itsOwnArtifactEntryGrantsFirstStrike() {
        Permanent destroyer = harness.enterBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void firstStrikeIsGrantedOnlyWhenTheTriggerResolves() {
        Permanent destroyer = harness.addToBattlefieldAndReturn(player1, new SkorpekhDestroyer());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(destroyer.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }
}
