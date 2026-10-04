package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GloomRipper.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
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

    @Test
    @DisplayName("Omitting the optional target leaves both creatures' toughness unchanged")
    void omittedTargetDoesNotReduceToughness() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Elf count uses resolution state, excludes opposing Elves, and stays fixed until cleanup")
    void countUsesResolutionStateAndExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, List.of(bear.getId(), elemental.getId()));
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);

        harness.setGraveyard(player1, List.of());
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reducing the opposing creature to zero toughness puts it into its graveyard")
    void toughnessReductionKillsOpposingCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new GloomRipper()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, List.of(bear.getId(), opposingBear.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
