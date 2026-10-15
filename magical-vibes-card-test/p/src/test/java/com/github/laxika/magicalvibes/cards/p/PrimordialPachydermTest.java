package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PrimordialPachyderm.class)
class PrimordialPachydermTest extends BaseCardTest {

    @Test
    @DisplayName("When Primordial Pachyderm enters, its controller gains 2 life")
    void gainsLifeWhenItEnters() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrimordialPachyderm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void gainsLifeWhenEnteringWithoutBeingCast() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.enterBattlefieldAndReturn(player2, new PrimordialPachyderm());
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Life is gained when the enter trigger resolves, not when the creature spell resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new PrimordialPachyderm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Pachyderm");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }
}
