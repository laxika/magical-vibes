package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BruteStrength.class, DuneBeetle.class, Plains.class})
class BruteStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Brute Strength cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Brute Strength");
    }

    @Test
    @DisplayName("Brute Strength can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Brute Strength");
    }

    @Test
    @DisplayName("Two Brute Strength boosts accumulate and both expire at end of turn")
    void repeatedBoostsAccumulateAndExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength(), new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting Brute Strength puts it on the stack targeting the creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Dune Beetle");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Brute Strength gives +3/+1 and trample to target creature")
    void resolvingBoostsAndGrantsTrample() {
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Dune Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent beetle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(beetle.getPowerModifier()).isEqualTo(3);
        assertThat(beetle.getToughnessModifier()).isEqualTo(1);
        assertThat(beetle.getEffectivePower()).isEqualTo(4);
        assertThat(beetle.getEffectiveToughness()).isEqualTo(5);
        assertThat(beetle.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Dune Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent beetle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(beetle.getPowerModifier()).isEqualTo(0);
        assertThat(beetle.getToughnessModifier()).isEqualTo(0);
        assertThat(beetle.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Brute Strength fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Dune Beetle");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Brute Strength");
    }
}
