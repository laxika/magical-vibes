package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CracklingCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives Crackling Cyclops +3/+0 until end of turn")
    void noncreatureSpellPumps() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a creature spell does not pump Crackling Cyclops")
    void creatureSpellDoesNotPump() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not pump Crackling Cyclops")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent cyclops = addCyclops();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    private Permanent addCyclops() {
        harness.addToBattlefield(player1, new CracklingCyclops());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Crackling Cyclops");
    }
}
