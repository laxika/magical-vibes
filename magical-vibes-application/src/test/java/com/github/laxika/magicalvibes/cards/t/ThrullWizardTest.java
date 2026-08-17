package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrullWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a black spell when its controller cannot pay")
    void countersBlackSpellWhenControllerCannotPay() {
        addReadyWizard();
        castDarkRitual();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("The black spell resolves when its controller pays three generic mana")
    void blackSpellResolvesWhenControllerPaysThreeGenericMana() {
        addReadyWizard();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castDarkRitual();

        harness.activateAbility(player1, 0, null, targetSpellId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonblack spell")
    void cannotTargetNonblackSpell() {
        addReadyWizard();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyWizard() {
        harness.addToBattlefield(player1, new ThrullWizard());
        findPermanent(player1, "Thrull Wizard").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castDarkRitual() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player2, List.of(ritual));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);
    }

    private java.util.UUID targetSpellId() {
        return gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Dark Ritual"))
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }
}
