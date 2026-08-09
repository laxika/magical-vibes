package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Phyrexian Driver")
class PhyrexianDriverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other Mercenary creatures on both sides")
    void etbBoostsOtherMercenaries() {
        Permanent ownMercenary = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        Permanent opponentMercenary = harness.addToBattlefieldAndReturn(player2, new DauthiMercenary());
        Permanent ownNonMercenary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PhyrexianDriver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent mercenary = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());

        harness.setHand(player1, List.of(new PhyrexianDriver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mercenary.getPowerModifier()).isEqualTo(0);
        assertThat(mercenary.getToughnessModifier()).isEqualTo(0);
    }
}
