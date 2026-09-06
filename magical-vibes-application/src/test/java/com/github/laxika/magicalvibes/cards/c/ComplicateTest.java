package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Complicate.class, GrizzlyBears.class, Shock.class})
class ComplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay 3")
    void countersSpellWhenControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Complicate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaves a spell on the stack when its controller pays 3")
    void leavesSpellOnStackWhenControllerPays() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Complicate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling optionally counters a spell and still draws")
    void cyclingAcceptCountersSpellAndDraws() {
        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Complicate()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();
        harness.activateHandAbility(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Complicate");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the cycling counter still draws and lets the spell resolve")
    void cyclingDeclineDrawsAndLeavesSpellUncountered() {
        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Complicate()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateHandAbility(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Complicate");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
