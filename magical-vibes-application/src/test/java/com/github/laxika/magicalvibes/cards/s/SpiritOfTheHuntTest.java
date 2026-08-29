package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Spirit of the Hunt boosts other Wolves and Werewolves you control")
    void enteringBoostsOtherWolvesAndWerewolves() {
        Permanent wolf = addCreatureReady(player1, new YoungWolf());
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingWolf = addCreatureReady(player2, new YoungWolf());

        castSpiritOfTheHunt();

        assertThat(wolf.getToughnessModifier()).isEqualTo(3);
        assertThat(werewolf.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(opposingWolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spirit of the Hunt does not boost itself")
    void enteringDoesNotBoostItself() {
        castSpiritOfTheHunt();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpiritOfTheHunt)
                .findFirst()
                .orElseThrow();

        assertThat(spirit.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spirit of the Hunt's boost ends at end of turn")
    void boostEndsAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new YoungWolf());

        castSpiritOfTheHunt();
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getToughnessModifier()).isZero();
    }

    private void castSpiritOfTheHunt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SpiritOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
