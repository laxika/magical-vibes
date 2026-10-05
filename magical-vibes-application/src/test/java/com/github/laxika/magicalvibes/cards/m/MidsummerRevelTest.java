package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MidsummerRevel.class)
class MidsummerRevelTest extends BaseCardTest {

    @Test
    @DisplayName("The upkeep trigger may add a verse counter")
    void upkeepTriggerMayAddVerseCounter() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(revel.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep trigger may be declined")
    void upkeepTriggerMayBeDeclined() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(revel.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Midsummer Revel creates one Beast per verse counter")
    void sacrificeCreatesBeastsEqualToVerseCounters() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());
        revel.setCounterCount(CounterType.VERSE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Midsummer Revel");
        assertThat(findPermanents(player1, "Beast")).hasSize(2);
        assertThat(findPermanents(player1, "Beast")).allSatisfy(beast -> {
            assertThat(beast.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(beast.getCard().getPower()).isEqualTo(3);
            assertThat(beast.getCard().getToughness()).isEqualTo(3);
            assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        });
    }

    @Test
    @DisplayName("Sacrificing Midsummer Revel with no verse counters creates no tokens")
    void sacrificeWithNoVerseCountersCreatesNoTokens() {
        harness.addToBattlefield(player1, new MidsummerRevel());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Midsummer Revel");
        assertThat(findPermanents(player1, "Beast")).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's upkeep adds a verse counter")
    void opponentsUpkeepDoesNotTrigger() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(revel.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    @DisplayName("Only verse counters determine the number of Beasts")
    void otherCounterTypesDoNotIncreaseTokenCount() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());
        revel.setCounterCount(CounterType.VERSE, 1);
        revel.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Midsummer Revel");
        harness.assertInGraveyard(player1, "Midsummer Revel");
        assertThat(findPermanents(player1, "Beast")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(findPermanents(player2, "Beast")).isEmpty();
    }

    @Test
    @DisplayName("Another Midsummer Revel does not supply counters to the sacrificed source")
    void tokenCountUsesOnlySacrificedSource() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());
        sacrificed.setCounterCount(CounterType.VERSE, 2);
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new MidsummerRevel());
        remaining.setCounterCount(CounterType.VERSE, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).hasSize(2);
        assertThat(findPermanents(player1, "Midsummer Revel")).containsExactly(remaining);
        assertThat(remaining.getCounterCount(CounterType.VERSE)).isEqualTo(5);
    }
}
