package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opposition;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuraFluxTest extends BaseCardTest {

    private Permanent addAuraFlux(Player controller) {
        Permanent auraFlux = new Permanent(new AuraFlux());
        gd.playerBattlefields.get(controller.getId()).add(auraFlux);
        return auraFlux;
    }

    private Permanent addOpposition(Player controller) {
        Permanent opposition = new Permanent(new Opposition());
        gd.playerBattlefields.get(controller.getId()).add(opposition);
        return opposition;
    }

    @Test
    @DisplayName("Declining to pay {2} sacrifices another enchantment")
    void decliningPaymentSacrificesAnotherEnchantment() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent opposition = addOpposition(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Opposition");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opposition);
    }

    @Test
    @DisplayName("Paying {2} keeps another enchantment on the battlefield")
    void payingKeepsAnotherEnchantment() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent opposition = addOpposition(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux, opposition);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Aura Flux does not tax itself")
    void doesNotTaxItself() {
        Permanent auraFlux = addAuraFlux(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux);
    }

    @Test
    @DisplayName("Aura Flux taxes enchantments controlled by an opponent")
    void taxesOpponentsEnchantment() {
        addAuraFlux(player2);
        addOpposition(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Opposition");
    }

    @Test
    @DisplayName("Aura Flux does not tax creatures")
    void doesNotTaxCreatures() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux, bears);
    }
}
