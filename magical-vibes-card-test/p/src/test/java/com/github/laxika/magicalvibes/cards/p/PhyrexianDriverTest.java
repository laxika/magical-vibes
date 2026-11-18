package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Phyrexian Driver")
@CardUsed({PhyrexianDriver.class, SpinelessThug.class, Mossdog.class})
class PhyrexianDriverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other Mercenary creatures on both sides")
    void etbBoostsOtherMercenaries() {
        Permanent ownMercenary = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        Permanent opponentMercenary = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        Permanent ownNonMercenary = harness.addToBattlefieldAndReturn(player1, new Mossdog());

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();

        Permanent driver = findPermanent(player1, "Phyrexian Driver");

        assertThat(PermanentGrants.powerBoostOf(gd, ownMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, ownNonMercenary)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownNonMercenary)).isEqualTo(0);
        assertThat(PermanentGrants.powerBoostOf(gd, driver)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, driver)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mercenary = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mercenary)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mercenary)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost does not affect Mercenaries that enter later")
    void boostDoesNotAffectLaterMercenaries() {
        Permanent existingMercenary = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();

        harness.castFromHand(player1, new SpinelessThug(), "{1}{B}");
        resolveAllTriggers();

        List<Permanent> mercenaries = findPermanents(player1, "Spineless Thug");
        assertThat(mercenaries).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, existingMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existingMercenary)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, mercenaries.getLast())).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mercenaries.getLast())).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Driver boosts the other Driver and boosts accumulate on other Mercenaries")
    void multipleDriversExcludeOnlyTheirOwnSource() {
        Permanent mercenary = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();
        Permanent firstDriver = findPermanent(player1, "Phyrexian Driver");

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();

        List<Permanent> drivers = findPermanents(player1, "Phyrexian Driver");
        assertThat(drivers).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, firstDriver)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstDriver)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, drivers.getLast())).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, drivers.getLast())).isEqualTo(0);
        assertThat(PermanentGrants.powerBoostOf(gd, mercenary)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mercenary)).isEqualTo(2);
    }
}
