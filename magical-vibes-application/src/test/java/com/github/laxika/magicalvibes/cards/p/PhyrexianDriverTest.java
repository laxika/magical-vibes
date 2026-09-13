package com.github.laxika.magicalvibes.cards.p;

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

        assertThat(ownMercenary.getPowerModifier()).isEqualTo(1);
        assertThat(ownMercenary.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentMercenary.getPowerModifier()).isEqualTo(1);
        assertThat(opponentMercenary.getToughnessModifier()).isEqualTo(1);
        assertThat(ownNonMercenary.getPowerModifier()).isEqualTo(0);
        assertThat(ownNonMercenary.getToughnessModifier()).isEqualTo(0);
        assertThat(driver.getPowerModifier()).isEqualTo(0);
        assertThat(driver.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mercenary = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());

        harness.castFromHand(player1, new PhyrexianDriver(), "{2}{B}");
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mercenary.getPowerModifier()).isEqualTo(0);
        assertThat(mercenary.getToughnessModifier()).isEqualTo(0);
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
        assertThat(existingMercenary.getPowerModifier()).isEqualTo(1);
        assertThat(existingMercenary.getToughnessModifier()).isEqualTo(1);
        assertThat(mercenaries.getLast().getPowerModifier()).isEqualTo(0);
        assertThat(mercenaries.getLast().getToughnessModifier()).isEqualTo(0);
    }
}
