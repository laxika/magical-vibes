package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatronWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Patron Wizard counters a spell when its controller cannot pay")
    void tapsWizardAndCountersSpellWhenControllerCannotPay() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, bears.getId());

        assertThat(patron.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The spell's controller may pay {1} to avoid being countered")
    void spellControllerMayPay() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped Patron Wizard cannot pay its own ability's Wizard cost")
    void tappedWizardCannotPayCost() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());
        patron.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
