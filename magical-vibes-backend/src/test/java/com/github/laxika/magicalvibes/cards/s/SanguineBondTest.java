package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanguineBondTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses life equal to life gained from ETB effect")
    void opponentLosesLifeOnLifeGain() {
        harness.addToBattlefield(player1, new SanguineBond());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Angel of Mercy ETB: gain 3 life
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve GainLifeEffect (life gain triggers Sanguine Bond)
        harness.passBothPriorities(); // resolve Sanguine Bond's triggered ability

        assertThat(gd.getLife(player1.getId())).isEqualTo(23); // 20 + 3
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger when opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new SanguineBond());

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        // Opponent gains life
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve GainLifeEffect

        // Player 1's life should be unchanged, player 2 gained 3 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore + 3);
    }

    @Test
    @DisplayName("Multiple Sanguine Bonds each trigger independently")
    void multipleSanguineBondsEachTrigger() {
        harness.addToBattlefield(player1, new SanguineBond());
        harness.addToBattlefield(player1, new SanguineBond());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Angel of Mercy ETB: gain 3 life
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve GainLifeEffect (both Sanguine Bonds trigger)
        harness.passBothPriorities(); // resolve first Sanguine Bond's triggered ability
        harness.passBothPriorities(); // resolve second Sanguine Bond's triggered ability

        assertThat(gd.getLife(player1.getId())).isEqualTo(23); // 20 + 3
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 6); // 3 + 3
    }

    @Test
    @DisplayName("Life loss amount matches exact life gained")
    void lifeLossMatchesExactLifeGained() {
        harness.addToBattlefield(player1, new SanguineBond());

        // Use Soul Warden + creature for 1 life gain
        harness.addToBattlefield(player1, new SoulWarden());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Cast a creature — Soul Warden triggers (gain 1 life)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (Soul Warden triggers)
        harness.passBothPriorities(); // resolve Soul Warden's GainLifeEffect (Sanguine Bond triggers)
        harness.passBothPriorities(); // resolve Sanguine Bond's triggered ability

        assertThat(gd.getLife(player1.getId())).isEqualTo(21); // 20 + 1
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }
}
