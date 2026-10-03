package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishSleeper;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoalitionSkyknight.class, BenalishSleeper.class, ColossalGrowth.class})
class CoalitionSkyknightTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player1, new CoalitionSkyknight());
        addCreatureReady(player2, new BenalishSleeper());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enlist cannot use creatures also declared as attackers")
    void enlistExcludesOtherAttackers() {
        Permanent first = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent second = addCreatureReady(player1, new CoalitionSkyknight());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist uses the supporter's power when the trigger resolves")
    void enlistUsesPowerAfterResponse() {
        Permanent skyknight = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent supporter = addCreatureReady(player1, new CoalitionSkyknight());
        harness.setHand(player1, List.of(new ColossalGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.castInstant(player1, 0, supporter.getId());
        harness.passBothPriorities();
        assertThat(supporter.getPowerModifier()).isEqualTo(3);
        assertThat(skyknight.getPowerModifier()).isZero();

        harness.passBothPriorities();
        assertThat(skyknight.getPowerModifier()).isEqualTo(5);
        assertThat(skyknight.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist can be declined without tapping the eligible supporter")
    void enlistCanBeDeclined() {
        Permanent skyknight = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent supporter = addCreatureReady(player1, new CoalitionSkyknight());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(supporter.isTapped()).isFalse();
        assertThat(skyknight.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enlist excludes tapped creatures and opposing creatures")
    void enlistExcludesTappedAndOpposingCreatures() {
        Permanent skyknight = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent tapped = addCreatureReady(player1, new CoalitionSkyknight());
        tapped.tap();
        addCreatureReady(player2, new CoalitionSkyknight());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(skyknight.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and gives Coalition Skyknight its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent skyknight = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent supporter = addCreatureReady(player1, new CoalitionSkyknight());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(supporter.isTapped()).isTrue();
        assertThat(skyknight.getPowerModifier()).isZero();

        harness.passBothPriorities();
        assertThat(skyknight.getPowerModifier()).isEqualTo(2);
        assertThat(skyknight.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist cannot use a creature with summoning sickness")
    void enlistExcludesSummoningSickCreature() {
        Permanent skyknight = addCreatureReady(player1, new CoalitionSkyknight());
        Permanent summoningSick = new Permanent(new CoalitionSkyknight());
        gd.playerBattlefields.get(player1.getId()).add(summoningSick);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(skyknight.getPowerModifier()).isZero();
    }
}
