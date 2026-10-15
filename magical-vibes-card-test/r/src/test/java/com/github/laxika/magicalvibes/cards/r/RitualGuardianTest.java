package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitualGuardian.class, CrawWurm.class, GrizzlyBears.class})
class RitualGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Coven grants lifelink when you control three creatures with different powers")
    void grantsLifelinkWithCoven() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not grant lifelink when your creatures do not have three different powers")
    void doesNotGrantLifelinkWithoutCoven() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentsCreaturesDoNotCountForCoven() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.forceActivePlayer(player2);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void rechecksCovenWhenTriggerResolves() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wurm));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void gainingCovenAfterCombatBeginsDoesNotCreateTrigger() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new CrawWurm());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void losingCovenAfterResolutionDoesNotRemoveLifelink() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wurm));

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void duplicatePowersDoNotPreventCovenWithThreeDistinctPowers() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();
    }
}
