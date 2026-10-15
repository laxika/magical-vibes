package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightyLeap.class, RuneclawBear.class, Forest.class, StormfrontPegasus.class})
class MightyLeapTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mighty Leap puts it on the stack")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MightyLeap.class);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Mighty Leap gives +2/+2 and flying to target creature")
    void resolvingBoostsAndGrantsFlying() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Mighty Leap fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Mighty Leap");
    }

    @Test
    @DisplayName("Mighty Leap can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Mighty Leap cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new RuneclawBear());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Mighty Leaps stack and cleanup preserves printed flying")
    void multipleLeapsStackAndPreservePrintedFlying() {
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new StormfrontPegasus());
        harness.setHand(player1, List.of(new MightyLeap(), new MightyLeap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, pegasus.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, pegasus.getId());
        harness.passBothPriorities();

        assertThat(pegasus.getEffectivePower()).isEqualTo(6);
        assertThat(pegasus.getEffectiveToughness()).isEqualTo(5);
        assertThat(pegasus.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(pegasus.getPowerModifier()).isZero();
        assertThat(pegasus.getToughnessModifier()).isZero();
        assertThat(pegasus.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
