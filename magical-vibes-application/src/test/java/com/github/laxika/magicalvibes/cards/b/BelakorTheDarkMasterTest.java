package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrinningDemon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelakorTheDarkMaster.class, GrinningDemon.class, GrizzlyBears.class})
class BelakorTheDarkMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws and loses life for each Demon controlled")
    void entersDrawsAndLosesLifeForEachDemon() {
        harness.addToBattlefield(player1, new GrinningDemon());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new BelakorTheDarkMaster());
        resolveStack();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another Demon entering deals its power to any target")
    void anotherDemonDealsItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new GrinningDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("A non-Demon entering does not trigger the damage ability")
    void nonDemonDoesNotTriggerDamageAbility() {
        harness.addToBattlefield(player1, new BelakorTheDarkMaster());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveStack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
