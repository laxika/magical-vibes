package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindingMage;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbigaleEloquentFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("ETB removes abilities and puts flying, first strike, and lifelink counters on another creature")
    void etbRemovesAbilitiesAndPutsKeywordCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BlindingMage());
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new AbigaleEloquentFirstYear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Keyword counters remain after the end-of-turn ability loss expires")
    void keywordCountersRemainAfterEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AbigaleEloquentFirstYear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("ETB can resolve without choosing a target")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new AbigaleEloquentFirstYear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Abigale, Eloquent First-Year");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AbigaleEloquentFirstYear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(harness.getPermanentId(player1, "Fountain of Youth"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
