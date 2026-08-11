package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KavuChameleonTest extends BaseCardTest {

    @Test
    @DisplayName("This spell can't be countered")
    void cannotBeCountered() {
        KavuChameleon kavu = new KavuChameleon();
        harness.setHand(player1, List.of(kavu));
        harness.addMana(player1, ManaColor.GREEN, 5);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, kavu.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Chameleon");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating {G} changes its color until end of turn")
    void activatingChangesItsColor() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuChameleon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("The chosen color wears off at end of turn")
    void chosenColorWearsOffAtEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuChameleon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate without paying {G}")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new KavuChameleon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
