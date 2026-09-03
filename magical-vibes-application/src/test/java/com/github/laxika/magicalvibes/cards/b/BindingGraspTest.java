package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindingGrasp.class, GrizzlyBears.class, FountainOfYouth.class})
class BindingGraspTest extends BaseCardTest {

    // ===== Control =====

    @Test
    @DisplayName("Resolving Binding Grasp steals the enchanted creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BindingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof BindingGrasp
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== +0/+1 boost =====

    @Test
    @DisplayName("Enchanted creature gets +0/+1")
    void enchantedCreatureGetsPlusZeroPlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        attach(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 1);
    }

    // ===== Upkeep sacrifice-unless-pay =====

    @Test
    @DisplayName("Declining to pay {1}{U} sacrifices Binding Grasp")
    void decliningPaymentSacrificesAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Binding Grasp");
        harness.assertInGraveyard(player1, "Binding Grasp");
    }

    @Test
    @DisplayName("Paying {1}{U} keeps Binding Grasp on the battlefield")
    void payingKeepsAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Binding Grasp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Binding Grasp")
    void insufficientManaSacrificesAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Binding Grasp");
        harness.assertInGraveyard(player1, "Binding Grasp");
    }

    @Test
    @DisplayName("Sacrificing Binding Grasp returns the creature to its previous controller")
    void sacrificingAuraReturnsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BindingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Binding Grasp");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Binding Grasp");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Binding Grasp")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BindingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent attach(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new BindingGrasp());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }
}
