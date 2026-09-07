package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DashHopes.class, GrizzlyBears.class})
class DashHopesTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when no player pays 5 life")
    void countersTargetSpellWhenNoPlayerPays() {
        GrizzlyBears bears = castTargetSpell();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    @Test
    @DisplayName("Any player may pay 5 life to counter Dash Hopes")
    void anyPlayerMayPayToCounterDashHopes() {
        castTargetSpell();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dash Hopes");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("A player with less than 5 life cannot pay")
    void playerWithInsufficientLifeCannotPay() {
        harness.setLife(player1, 4);
        harness.setLife(player2, 4);
        castTargetSpell();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    private GrizzlyBears castTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DashHopes()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        return bears;
    }
}
