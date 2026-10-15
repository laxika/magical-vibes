package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoporritScout.class})
class LoporritScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when another creature you control enters")
    void anotherCreatureEnteringBoosts() {
        Permanent scout = addScout();
        castScout(player1);

        assertThat(scout.getPowerModifier()).isEqualTo(1);
        assertThat(scout.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Its own entry does not trigger the ability")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new LoporritScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scout = findPermanent(player1, "Loporrit Scout");
        assertThat(scout.getPowerModifier()).isEqualTo(0);
        assertThat(scout.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent scout = addScout();
        castScout(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scout.getPowerModifier()).isEqualTo(0);
        assertThat(scout.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger it")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent scout = addScout();
        harness.setHand(player2, List.of(new LoporritScout()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(scout.getPowerModifier()).isEqualTo(0);
        assertThat(scout.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each additional creature entry gives a separate cumulative boost")
    void multipleEntriesGiveCumulativeBoosts() {
        Permanent scout = addScout();
        castScout(player1);
        castScout(player1);

        assertThat(scout.getPowerModifier()).isEqualTo(2);
        assertThat(scout.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Scout boosts only the Scout already on the battlefield")
    void anotherScoutEnteringBoostsOnlyExistingScout() {
        Permanent firstScout = addScout();
        harness.setHand(player1, List.of(new LoporritScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent secondScout = findPermanents(player1, "Loporrit Scout").stream()
                .filter(permanent -> !permanent.getId().equals(firstScout.getId()))
                .findFirst().orElseThrow();
        assertThat(firstScout.getPowerModifier()).isEqualTo(1);
        assertThat(firstScout.getToughnessModifier()).isEqualTo(1);
        assertThat(secondScout.getPowerModifier()).isZero();
        assertThat(secondScout.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost waits for the triggered ability to resolve")
    void boostIsNotAppliedBeforeTriggerResolves() {
        Permanent scout = addScout();
        harness.setHand(player1, List.of(new LoporritScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Loporrit Scout")).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(scout.getPowerModifier()).isZero();
        assertThat(scout.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(scout.getPowerModifier()).isEqualTo(1);
        assertThat(scout.getToughnessModifier()).isEqualTo(1);
    }
    private Permanent addScout() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LoporritScout());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return scout;
    }

    private void castScout(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new LoporritScout()));
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }
}
