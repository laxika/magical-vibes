package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinWarWagon.class})
class GoblinWarWagonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Goblin War Wagon does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        harness.performUntapStep(player1);

        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {2} during upkeep untaps Goblin War Wagon")
    void payingTwoUntapsGoblinWarWagon() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wagon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Goblin War Wagon tapped")
    void decliningLeavesGoblinWarWagonTapped() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Goblin War Wagon does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the upkeep payment without enough mana leaves Goblin War Wagon tapped")
    void acceptingWithoutTwoManaLeavesGoblinWarWagonTapped() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(wagon.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying Goblin War Wagon's upkeep cost consumes exactly two mana")
    void payingTwoConsumesExactlyTwoMana() {
        Permanent wagon = addCreatureReady(player1, new GoblinWarWagon());
        wagon.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(wagon.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
