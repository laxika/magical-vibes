package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErraticCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant gives Erratic Cyclops +X/+0 based on its mana value")
    void instantSpellBoostsPowerOnly() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(8);
    }

    @Test
    @DisplayName("Casting a sorcery gives Erratic Cyclops +X/+0 based on its mana value")
    void sorcerySpellBoostsPowerOnly() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(8);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Erratic Cyclops")
    void creatureSpellDoesNotTrigger() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(8);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cyclops = addCyclops();
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(8);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Erratic Cyclops")
    void opponentInstantDoesNotTrigger() {
        Permanent cyclops = addCyclops();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(8);
    }

    private Permanent addCyclops() {
        harness.addToBattlefield(player1, new ErraticCyclops());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Erratic Cyclops");
    }
}
