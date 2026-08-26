package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrullWizard.class, BasalThrull.class, IcatianInfantry.class})
class ThrullWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a black spell when its controller cannot pay")
    void countersBlackSpellWhenControllerCannotPay() {
        addReadyWizard();
        castBasalThrull();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Basal Thrull");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Counters the black spell when its controller declines a payment")
    void countersBlackSpellWhenControllerDeclinesPayment() {
        addReadyWizard();
        harness.addMana(player2, ManaColor.BLACK, 1);
        castBasalThrull();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Basal Thrull");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can activate while tapped because the ability has no tap cost")
    void canActivateWhileTapped() {
        Permanent wizard = addReadyWizard();
        wizard.tap();
        castBasalThrull();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Basal Thrull");
    }

    @Test
    @DisplayName("The black spell resolves when its controller pays one black mana")
    void blackSpellResolvesWhenControllerPaysOneBlackMana() {
        addReadyWizard();
        harness.addMana(player2, ManaColor.BLACK, 1);
        castBasalThrull();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black spell resolves when its controller pays three generic mana")
    void blackSpellResolvesWhenControllerPaysThreeGenericMana() {
        addReadyWizard();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castBasalThrull();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonblack spell")
    void cannotTargetNonblackSpell() {
        addReadyWizard();
        IcatianInfantry infantry = new IcatianInfantry();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, infantry, "{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, infantry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWizard() {
        Permanent wizard = addCreatureReady(player1, new ThrullWizard());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return wizard;
    }

    private void castBasalThrull() {
        BasalThrull basalThrull = new BasalThrull();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, basalThrull, "{B}{B}");
        harness.passPriority(player2);
    }

    private java.util.UUID targetSpellId() {
        return gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Basal Thrull"))
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }
}
