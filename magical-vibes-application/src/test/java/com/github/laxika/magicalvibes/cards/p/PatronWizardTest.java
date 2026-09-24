package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatronWizard.class, AvenFlock.class})
class PatronWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Patron Wizard counters a spell when its controller cannot pay")
    void tapsWizardAndCountersSpellWhenControllerCannotPay() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        AvenFlock spell = new AvenFlock();
        harness.castFromHand(player1, spell, "{4}{W}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());

        assertThat(patron.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Flock");
        harness.assertNotOnBattlefield(player1, "Aven Flock");
    }

    @Test
    @DisplayName("The spell's controller may pay {1} to avoid being countered")
    void spellControllerMayPay() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        AvenFlock spell = new AvenFlock();
        harness.castFromHand(player1, spell, "{4}{W}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aven Flock");
    }

    @Test
    @DisplayName("Another untapped Wizard may pay the ability's cost")
    void anotherWizardMayPayCost() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());
        Permanent otherWizard = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        AvenFlock spell = new AvenFlock();
        harness.castFromHand(player1, spell, "{4}{W}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, otherWizard.getId());

        assertThat(patron.isTapped()).isFalse();
        assertThat(otherWizard.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Flock");
    }

    @Test
    @DisplayName("A tapped source may use another untapped Wizard to pay its cost")
    void tappedSourceMayUseAnotherWizard() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());
        patron.tap();
        Permanent otherWizard = harness.addToBattlefieldAndReturn(player2, new PatronWizard());

        AvenFlock spell = new AvenFlock();
        harness.castFromHand(player1, spell, "{4}{W}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, spell.getId());

        assertThat(patron.isTapped()).isTrue();
        assertThat(otherWizard.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Flock");
    }

    @Test
    @DisplayName("A tapped Patron Wizard cannot pay its own ability's Wizard cost")
    void tappedWizardCannotPayCost() {
        Permanent patron = harness.addToBattlefieldAndReturn(player2, new PatronWizard());
        patron.tap();
        harness.addToBattlefield(player2, new AvenFlock());

        AvenFlock spell = new AvenFlock();
        harness.castFromHand(player1, spell, "{4}{W}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
