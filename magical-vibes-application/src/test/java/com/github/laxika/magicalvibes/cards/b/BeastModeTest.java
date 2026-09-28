package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastMode.class, GrizzlyBears.class, Forest.class})
class BeastModeTest extends BaseCardTest {

    @Test
    void boostsAndGrantsTrampleWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(target, List.of());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void teamworkAlsoPutsACounterOnTheTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void temporaryBoostWearsOffButTeamworkCounterRemains() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(teammate.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BeastMode()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target, List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new BeastMode()));
        addMana();
        harness.castInstantWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
