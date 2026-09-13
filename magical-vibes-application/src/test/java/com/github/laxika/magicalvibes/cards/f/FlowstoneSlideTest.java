package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneSlide.class, GrizzlyBears.class, AirElemental.class, Forest.class})
class FlowstoneSlideTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Flowstone Slide puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7); // X=3: {3}{2}{R}{R} = 7 total

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mana is fully consumed when casting")
    void manaIsConsumedWhenCasting() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("All creatures on both sides get +X/-X")
    void allCreaturesGetBoost() {
        // 2/2 on each side
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1: {1}{2}{R}{R} = 5

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // Both bears should be 3/1 (2+1 / 2-1)
        assertThat(bear1.getPowerModifier()).isEqualTo(1);
        assertThat(bear1.getToughnessModifier()).isEqualTo(-1);
        assertThat(bear2.getPowerModifier()).isEqualTo(1);
        assertThat(bear2.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("X=2 kills 2-toughness creatures via state-based actions")
    void killsSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 6); // X=2: {2}{2}{R}{R} = 6

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Both 2/2 bears get +2/-2 → 4/0 → die to SBA
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Large X kills small creatures but not large ones")
    void largeXKillsSmallButNotLargeCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        Permanent bigCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 7); // X=3: {3}{2}{R}{R} = 7

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        // 2/2 bear gets +3/-3 → 5/-1 → dies
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // 4/4 gets +3/-3 → 7/1 → survives
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(bigCreature.getPowerModifier()).isEqualTo(3);
        assertThat(bigCreature.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("X=0 resolves with no effect on creatures")
    void xZeroHasNoEffect() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 4); // X=0: {0}{2}{R}{R} = 4

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Bear is unchanged
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Noncreature permanents are not affected")
    void doesNotAffectNoncreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1: {1}{2}{R}{R} = 5

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(forest.getPowerModifier()).isZero();
        assertThat(forest.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 5); // X=1: {1}{2}{R}{R} = 5

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Resolution logs the boost")
    void resolutionLogsBoost() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Flowstone Slide") && log.contains("creature"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot cast without enough mana for base cost plus X")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FlowstoneSlide()));
        harness.addMana(player1, ManaColor.RED, 4); // Only enough for X=0

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}

