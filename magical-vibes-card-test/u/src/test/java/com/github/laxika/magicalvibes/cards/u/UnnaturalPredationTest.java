package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RotWolf;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
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

@CardUsed({UnnaturalPredation.class, RotWolf.class, IchorWellspring.class})
class UnnaturalPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Unnatural Predation puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Rot Wolf");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unnatural Predation");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Unnatural Predation gives +1/+1 and trample to target creature")
    void resolvingBoostsAndGrantsTrample() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Rot Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wolf.getPowerModifier()).isEqualTo(1);
        assertThat(wolf.getToughnessModifier()).isEqualTo(1);
        assertThat(wolf.getEffectivePower()).isEqualTo(3);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(3);
        assertThat(wolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Rot Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
        assertThat(wolf.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Unnatural Predation fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Rot Wolf");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Unnatural Predation");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Unnatural Predation");
    }

    @Test
    @DisplayName("Multiple casts stack their boosts and all expire at cleanup")
    void repeatedCastsStackUntilCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        harness.setHand(player1, List.of(new UnnaturalPredation(), new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorWellspring());
        harness.setHand(player1, List.of(new UnnaturalPredation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(artifact.getPowerModifier()).isZero();
        assertThat(artifact.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
