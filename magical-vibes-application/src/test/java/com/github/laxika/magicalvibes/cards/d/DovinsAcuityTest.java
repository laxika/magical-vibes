package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RootSnare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DovinsAcuityTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, you gain 2 life and draw a card")
    void entersGainsLifeAndDrawsCard() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new DovinsAcuity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("May return to hand when you cast an instant during your main phase")
    void mayReturnToHandForInstantDuringMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.setHand(player1, List.of(new RootSnare()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dovin's Acuity");
        harness.assertNotOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Does not return when the instant is cast outside your main phase")
    void doesNotReturnForInstantOutsideMainPhase() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.setHand(player1, List.of(new RootSnare()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dovin's Acuity");
    }

    @Test
    @DisplayName("Does not return when you cast a creature during your main phase")
    void doesNotReturnForCreatureSpell() {
        harness.addToBattlefield(player1, new DovinsAcuity());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dovin's Acuity");
    }
}
