package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoporritScout.class, GrizzlyBears.class})
class LoporritScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when another creature you control enters")
    void anotherCreatureEnteringBoosts() {
        Permanent scout = addScout();
        castBears(player1);

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
        castBears(player1);

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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(scout.getPowerModifier()).isEqualTo(0);
        assertThat(scout.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addScout() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LoporritScout());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return scout;
    }

    private void castBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
