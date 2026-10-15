package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CandlegroveWitch.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class, UnrulyMob.class, CelestusSanctifier.class})
class CandlegroveWitchTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }

    @Test
    @DisplayName("Coven grants flying when you control three creatures with different powers")
    void grantsFlyingWithCoven() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flying when your creatures do not have three different powers")
    void doesNotGrantFlyingWithoutCoven() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Coven does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new CelestusSanctifier());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures do not count toward coven")
    void opponentsCreaturesDoNotCount() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player2, new UnrulyMob());
        harness.addToBattlefield(player2, new CelestusSanctifier());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Coven must still be met when the ability resolves")
    void covenIsRecheckedAtResolution() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new UnrulyMob());
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new CelestusSanctifier());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        sanctifier.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Establishing coven after combat begins does not create a trigger")
    void gainingCovenAfterCombatBeginsDoesNotTrigger() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new CelestusSanctifier());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Duplicate powers do not prevent coven when three distinct powers exist")
    void extraCreatureWithDuplicatePowerDoesNotPreventCoven() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent otherWitch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent mob = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new CelestusSanctifier());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWitch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, mob, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sanctifier, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Losing coven after resolution does not remove granted flying")
    void flyingPersistsAfterCovenIsLost() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new UnrulyMob());
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new CelestusSanctifier());

        advanceToCombat(player1);
        harness.passBothPriorities();
        sanctifier.setPowerModifier(-1);

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();
    }
}
