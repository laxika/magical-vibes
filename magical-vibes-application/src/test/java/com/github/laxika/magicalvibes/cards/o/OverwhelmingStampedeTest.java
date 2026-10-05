package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.StabbingPain;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverwhelmingStampede.class, GrizzlyBears.class, HillGiant.class,
        GiantGrowth.class, StabbingPain.class, WallOfFrost.class})
class OverwhelmingStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Boost is based on the greatest power among controlled creatures")
    void boostBasedOnGreatestPower() {
        // HillGiant is 3/3, GrizzlyBears is 2/2 — greatest power is 3
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingStampede()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        // HillGiant: 3+3=6/3+3=6, GrizzlyBears: 2+3=5/2+3=5
        assertThat(hillGiant.getEffectivePower()).isEqualTo(6);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(6);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);

        assertThat(hillGiant.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingStampede()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("With no creatures, boost is +0/+0")
    void noCreaturesGivesZeroBoost() {
        // Cast with no creatures on the battlefield
        harness.setHand(player1, List.of(new OverwhelmingStampede()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Spell resolves without error, no creatures to boost
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingStampede()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getEffectivePower()).isEqualTo(4);  // 2 + 2 (greatest power is 2)
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A negative greatest power reduces power and toughness")
    void negativeGreatestPowerIsNotClampedToZero() {
        Permanent wall = addCreatureReady(player1, new WallOfFrost());
        harness.setHand(player1, List.of(new StabbingPain(), new OverwhelmingStampede()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, wall.getId());
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
        assertThat(wall.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Greatest power is measured at resolution, including a response")
    void greatestPowerIncludesPumpResolvedInResponse() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new OverwhelmingStampede(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(8);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(giant.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive either effect")
    void laterCreatureDoesNotReceiveBoostOrTrample() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingStampede(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent later = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(3);
        assertThat(later.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
