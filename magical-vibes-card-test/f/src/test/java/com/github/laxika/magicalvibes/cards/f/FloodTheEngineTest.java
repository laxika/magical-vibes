package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodTheEngine.class, AirResponseUnit.class, BrightfieldGlider.class, Forest.class, SnakeskinVeil.class})
class FloodTheEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Flood the Engine taps an enchanted creature when it enters")
    void tapsEnchantedCreatureWhenItEnters() {
        Permanent creature = addCreatureReady(player2, new BrightfieldGlider());

        castFloodTheEngine(creature);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flood the Engine can enchant a Vehicle and removes its abilities")
    void enchantsVehicleAndRemovesItsAbilities() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        addCreatureReady(player2, new BrightfieldGlider());

        castFloodTheEngine(vehicle);

        assertThat(vehicle.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Flood the Engine keeps the enchanted permanent tapped through its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        castFloodTheEngine(vehicle);

        harness.performUntapStep(player2);

        assertThat(vehicle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flood the Engine cannot enchant a noncreature non-Vehicle permanent")
    void cannotEnchantOtherPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FloodTheEngine()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    private void castFloodTheEngine(Permanent target) {
        harness.setHand(player1, List.of(new FloodTheEngine()));
        addCastingMana();

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Flood the Engine removes a creature's vigilance")
    void removesCreatureKeywords() {
        Permanent creature = addCreatureReady(player2, new BrightfieldGlider());

        castFloodTheEngine(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Flood the Engine removes an uncrewed Vehicle's keywords")
    void removesUncrewedVehicleKeywords() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());

        castFloodTheEngine(vehicle);

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("The enter trigger taps the enchanted creature even if it gains hexproof in response")
    void enterTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new BrightfieldGlider());
        harness.setHand(player1, List.of(new FloodTheEngine()));
        addCastingMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player2, List.of(new SnakeskinVeil()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted permanent is prevented from untapping")
    void otherPermanentsUntapNormally() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        Permanent other = addCreatureReady(player2, new BrightfieldGlider());
        other.tap();
        castFloodTheEngine(enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }
}
