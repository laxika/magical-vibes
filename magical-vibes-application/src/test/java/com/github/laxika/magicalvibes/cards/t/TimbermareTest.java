package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Timbermare.class, GrizzlyBears.class})
class TimbermareTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures, including creatures its controller does not control")
    void entersAndTapsAllOtherCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveTimbermare();

        Permanent timbermare = findPermanent(player1, "Timbermare");
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(timbermare.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining echo sacrifices Timbermare at its next upkeep")
    void decliningEchoSacrificesTimbermare() {
        castAndResolveTimbermare();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Timbermare");
        harness.assertInGraveyard(player1, "Timbermare");
    }

    @Test
    @DisplayName("Paying echo keeps Timbermare and echo does not trigger again")
    void payingEchoKeepsTimbermareAndIsOneShot() {
        castAndResolveTimbermare();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Timbermare");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Timbermare");
    }

    private void castAndResolveTimbermare() {
        harness.setHand(player1, List.of(new Timbermare()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Timbermare");
    }
}
