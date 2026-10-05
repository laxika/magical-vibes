package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinebreakerBaloth.class, GrizzlyBears.class, HillGiant.class, ColossalGrowth.class})
class LinebreakerBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Linebreaker Baloth cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        baloth.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Linebreaker Baloth can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        baloth.setAttacking(true);

        addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Linebreaker Baloth by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(baloth.getPowerModifier()).isEqualTo(2);
        assertThat(baloth.getToughnessModifier()).isZero();
    }

    @Test
    void enlistCanBeDeclined() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new LinebreakerBaloth());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(baloth.getPowerModifier()).isZero();
    }

    @Test
    void enlistExcludesTappedSummoningSickAttackingAndOpposingCreatures() {
        addCreatureReady(player1, new LinebreakerBaloth());
        Permanent tapped = addCreatureReady(player1, new LinebreakerBaloth());
        tapped.tap();
        Permanent sick = addCreatureReady(player1, new LinebreakerBaloth());
        sick.setSummoningSick(true);
        addCreatureReady(player1, new LinebreakerBaloth());
        Permanent eligible = addCreatureReady(player1, new LinebreakerBaloth());
        addCreatureReady(player2, new LinebreakerBaloth());

        declareAttackers(List.of(0, 3));
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(eligible.isTapped()).isTrue();
        assertThat(sick.isTapped()).isFalse();
    }

    @Test
    void enlistUsesSupporterPowerWhenTriggerResolves() {
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new LinebreakerBaloth());
        harness.setHand(player1, List.of(new ColossalGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
            assertThat(baloth.getPowerModifier()).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, supporter.getId());
            harness.passBothPriorities();
            assertThat(supporter.getPowerModifier()).isEqualTo(3);
            harness.passBothPriorities();
        });

        assertThat(baloth.getPowerModifier()).isEqualTo(7);
        assertThat(baloth.getToughnessModifier()).isZero();
    }

    @Test
    void boostedSmallCreatureCanBlock() {
        addCreatureReady(player1, new LinebreakerBaloth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new ColossalGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player2, 0, blocker.getId());
            harness.passBothPriorities();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }
}
