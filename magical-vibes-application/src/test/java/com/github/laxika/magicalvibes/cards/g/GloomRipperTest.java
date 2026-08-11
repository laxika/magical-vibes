package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GloomRipperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts a creature you control and weakens an opposing creature by the Elf count")
    void etbUsesControlledAndGraveyardElfCount() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, List.of(bear.getId(), elemental.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The opposing creature target is optional")
    void opposingCreatureTargetIsOptional() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void firstTargetMustBeControlledCreature() {
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentCreature)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
