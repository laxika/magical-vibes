package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurgingStormbrood.class, Forest.class, GrizzlyBears.class})
class PurgingStormbroodTest extends BaseCardTest {

    @Test
    @DisplayName("ETB removes all counters from up to one target creature")
    void etbRemovesAllCountersFromTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new PurgingStormbrood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new PurgingStormbrood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Purging Stormbrood");
    }

    @Test
    @DisplayName("Omen boosts a creature, grants lifelink and hexproof, and shuffles into its owner's library")
    void omenBoostsCreatureAndShuffles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PurgingStormbrood card = new PurgingStormbrood();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Omen's temporary effects wear off at cleanup")
    void omenEffectsWearOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PurgingStormbrood()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Omen only targets creatures")
    void omenRejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PurgingStormbrood()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
