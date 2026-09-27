package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GoblinDirigible.class)
class GoblinDirigibleTest extends BaseCardTest {

    @Test
    void doesNotUntapDuringUntapStep() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        harness.performUntapStep(player1);

        assertThat(dirigible.isTapped()).isTrue();
    }

    @Test
    void payingFourDuringUpkeepUntapsDirigible() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dirigible.isTapped()).isFalse();
    }

    @Test
    void decliningUpkeepPaymentLeavesDirigibleTapped() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dirigible.isTapped()).isTrue();
    }

    @Test
    void payingFourConsumesOnlyFourMana() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(dirigible.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void acceptingWithoutFourManaLeavesDirigibleTapped() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(dirigible.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent dirigible = addCreatureReady(player1, new GoblinDirigible());
        dirigible.tap();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(dirigible.isTapped()).isTrue();
    }
}
