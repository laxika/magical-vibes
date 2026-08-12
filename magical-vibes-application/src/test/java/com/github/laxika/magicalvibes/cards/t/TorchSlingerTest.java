package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TorchSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not deal damage")
    void withoutKickerDoesNotDealDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TorchSlinger()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Torch Slinger");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB ability deals 2 damage to a target creature")
    void kickedDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TorchSlinger()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
