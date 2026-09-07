package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UktabiDrake.class)
class UktabiDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Uktabi Drake at its next upkeep")
    void decliningEchoSacrificesUktabiDrake() {
        castAndResolveUktabiDrake();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Uktabi Drake");
        harness.assertInGraveyard(player1, "Uktabi Drake");
    }

    @Test
    @DisplayName("Paying echo keeps Uktabi Drake and echo does not trigger again")
    void payingEchoKeepsUktabiDrakeAndIsOneShot() {
        castAndResolveUktabiDrake();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Uktabi Drake");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Uktabi Drake");
    }

    private void castAndResolveUktabiDrake() {
        harness.setHand(player1, List.of(new UktabiDrake()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Uktabi Drake");
    }
}
