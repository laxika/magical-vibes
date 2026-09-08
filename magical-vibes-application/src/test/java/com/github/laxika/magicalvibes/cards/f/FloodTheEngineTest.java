package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodTheEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Flood the Engine taps an enchanted creature when it enters")
    void tapsEnchantedCreatureWhenItEnters() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castFloodTheEngine(creature);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flood the Engine can enchant a Vehicle and removes its abilities")
    void enchantsVehicleAndRemovesItsAbilities() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        addCreatureReady(player2, new GrizzlyBears());

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

        advanceToNextTurn(player1);

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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
