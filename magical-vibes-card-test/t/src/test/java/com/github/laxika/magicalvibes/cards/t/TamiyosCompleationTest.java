package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RabbitBattery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosCompleation.class, AirElemental.class, GrizzlyBears.class, JaceBeleren.class,
        LeoninScimitar.class, Boomerang.class, RabbitBattery.class})
class TamiyosCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Tamiyo's Compleation to be cast during an opponent's turn")
    void canCastAtInstantSpeed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Entering Tamiyo's Compleation taps an enchanted permanent")
    void enteringAuraTapsEnchantedPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering Tamiyo's Compleation unattaches an enchanted Equipment")
    void enteringAuraUnattachesEnchantedEquipment() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, equipment.getId());
        resolveAllTriggers();

        assertThat(equipment.isTapped()).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Enchanted permanents lose abilities and do not untap")
    void enchantedPermanentLosesAbilitiesAndDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, com.github.laxika.magicalvibes.model.Keyword.FLYING))
                .isFalse();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tamiyo's Compleation can enchant a planeswalker")
    void canEnchantPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Tamiyo's Compleation").getAttachedTo())
                .isEqualTo(planeswalker.getId());
    }

    @Test
    @DisplayName("Tamiyo's Compleation cannot enchant an enchantment")
    void cannotEnchantEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new TamiyosCompleation());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or planeswalker");
    }

    private void addManaForTamiyosCompleation() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Equipment loses its bonus immediately but remains attached until the entry trigger resolves")
    void equipmentLosesAbilitiesBeforeEntryTriggerResolves() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        resolveAllTriggers();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(equipment.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The entry trigger still unattaches Equipment after the Aura is returned to hand")
    void entryTriggerUnattachesEquipmentAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Tamiyo's Compleation");

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Tamiyo's Compleation");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        resolveAllTriggers();

        assertThat(equipment.isTapped()).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Attached reconfigure Equipment stays a noncreature until the entry trigger unattaches it")
    void reconfigureEquipmentBecomesCreatureOnlyAfterUnattaching() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent battery = addCreatureReady(player2, new RabbitBattery());
        battery.setAttachedTo(creature.getId());
        assertThat(gqs.isCreature(gd, battery)).isFalse();

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, battery.getId());
        harness.passBothPriorities();

        assertThat(battery.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, battery)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(battery.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, battery)).isTrue();
        assertThat(battery.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, battery, com.github.laxika.magicalvibes.model.Keyword.HASTE))
                .isFalse();
        harness.performUntapStep(player2);
        assertThat(battery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura restores abilities and allows normal untapping")
    void removingAuraRestoresAbilitiesAndUntapping() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Tamiyo's Compleation").getId());

        assertThat(gqs.hasKeyword(gd, creature, com.github.laxika.magicalvibes.model.Keyword.FLYING))
                .isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted planeswalkers cannot activate loyalty abilities")
    void enchantedPlaneswalkerCannotActivateLoyaltyAbility() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
