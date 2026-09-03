package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrashOfRhinos;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.u.UnyaroBeeSting;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindingAgony.class, BenevolentUnicorn.class, CrashOfRhinos.class,
        Forest.class, Incinerate.class, RayOfCommand.class, UnyaroBeeSting.class})
class BindingAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage to enchanted creature deals that much to its controller")
    void spellDamageDealsEqualDamageToController() {
        Permanent creature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 2);
    }

    @Test
    @DisplayName("Damage dealt to controller matches the amount of damage received")
    void damageAmountMatchesDamageReceived() {
        Permanent creature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("No trigger when the enchanted creature is not dealt damage")
    void noTriggerWithoutDamage() {
        Permanent creature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to a different creature does not trigger the Aura")
    void damageToDifferentCreatureDoesNotTrigger() {
        Permanent enchantedCreature = addCreatureReady(player2, new CrashOfRhinos());
        Permanent otherCreature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Combat damage triggers even when the enchanted creature dies")
    void combatDamageTriggersWhenEnchantedCreatureDies() {
        addCreatureReady(player1, new CrashOfRhinos());
        Permanent enchantedCreature = addCreatureReady(player2, new BenevolentUnicorn());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantedCreature);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 8);
    }

    @Test
    @DisplayName("The reflected damage uses the actual damage dealt, not the original spell amount")
    void reflectedDamageUsesActualDamage() {
        Permanent enchantedCreature = addCreatureReady(player2, new CrashOfRhinos());
        addCreatureReady(player2, new BenevolentUnicorn());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        int controllerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLifeBefore - 2);
    }

    @Test
    @DisplayName("The controller is determined when the triggered ability resolves")
    void usesCurrentControllerWhenTriggerResolves() {
        Permanent enchantedCreature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Binding Agony cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);

        harness.setHand(player1, List.of(new BindingAgony()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
