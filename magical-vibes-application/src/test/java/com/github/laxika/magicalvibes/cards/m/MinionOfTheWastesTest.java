package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinionOfTheWastesTest extends BaseCardTest {

    private void cast(String lifePaid) {
        harness.setHand(player1, List.of(new MinionOfTheWastes()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (lifePaid != null) {
            harness.handleListChoice(player1, lifePaid);
        }
    }

    @Test
    @DisplayName("Entering awaits a life payment choice")
    void enteringAwaitsLifePayment() {
        cast(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.PayAnyAmountOfLifeAsEnters.class);
    }

    @Test
    @DisplayName("Paying 5 life makes it a 5/5 and costs 5 life")
    void payingFiveLife() {
        harness.setLife(player1, 20);

        cast("5");

        Permanent minion = findPermanent(player1, "Minion of the Wastes");
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Paying 0 life leaves a 0/0 that dies to state-based actions")
    void payingZeroLifeDies() {
        harness.setLife(player1, 20);

        cast("0");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Minion of the Wastes"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Minion of the Wastes"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot pay more life than the controller has")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 4);

        cast(null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1", "2", "3", "4");
    }
}
