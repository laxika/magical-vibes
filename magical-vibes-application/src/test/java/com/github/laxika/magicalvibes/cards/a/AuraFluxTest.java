package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.Knighthood;
import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuraFlux.class, Knighthood.class, PlagueBeetle.class})
class AuraFluxTest extends BaseCardTest {

    private Permanent addAuraFlux(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new AuraFlux());
    }

    private Permanent addKnighthood(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new Knighthood());
    }

    @Test
    @DisplayName("Declining to pay {2} sacrifices another enchantment")
    void decliningPaymentSacrificesAnotherEnchantment() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent knighthood = addKnighthood(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Knighthood");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knighthood);
    }

    @Test
    @DisplayName("Paying {2} keeps another enchantment on the battlefield")
    void payingKeepsAnotherEnchantment() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent knighthood = addKnighthood(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux, knighthood);
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
    @DisplayName("Aura Flux does not grant abilities while it has no abilities")
    void doesNotGrantAbilitiesWhileItHasNoAbilities() {
        Permanent auraFlux = addAuraFlux(player1);
        auraFlux.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent knighthood = addKnighthood(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux, knighthood);
    }

    @Test
    @DisplayName("Aura Flux taxes enchantments controlled by an opponent")
    void taxesOpponentsEnchantment() {
        addAuraFlux(player2);
        addKnighthood(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Knighthood");
    }

    @Test
    @DisplayName("Aura Flux does not tax creatures")
    void doesNotTaxCreatures() {
        Permanent auraFlux = addAuraFlux(player1);
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new PlagueBeetle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auraFlux, beetle);
    }

    @Test
    @DisplayName("Two Aura Fluxes tax each other")
    void twoAuraFluxesTaxEachOther() {
        Permanent firstAuraFlux = addAuraFlux(player1);
        Permanent secondAuraFlux = addAuraFlux(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstAuraFlux, secondAuraFlux);
    }

    @Test
    @DisplayName("Multiple Aura Fluxes give another enchantment multiple upkeep abilities")
    void multipleAuraFluxesGiveAnotherEnchantmentMultipleUpkeepAbilities() {
        Permanent firstAuraFlux = addAuraFlux(player1);
        Permanent secondAuraFlux = addAuraFlux(player1);
        Permanent knighthood = addKnighthood(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 8);

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction())
                    .as("upkeep ability %s", i + 1)
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            if (i < 3) {
                harness.passBothPriorities();
            }
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstAuraFlux, secondAuraFlux, knighthood);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
