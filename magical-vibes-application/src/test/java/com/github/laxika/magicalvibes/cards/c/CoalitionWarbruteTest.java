package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalitionWarbrute.class, GrizzlyBears.class, ColossalGrowth.class})
class CoalitionWarbruteTest extends BaseCardTest {

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Coalition Warbrute by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(warbrute.getPowerModifier()).isEqualTo(2);
        assertThat(warbrute.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist cannot use a creature with summoning sickness")
    void enlistExcludesSummoningSickCreature() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(warbrute.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist uses the supporter's power when its trigger resolves")
    void enlistUsesPowerAtResolution() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new CoalitionWarbrute());
        harness.setHand(player1, List.of(new ColossalGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(warbrute.getPowerModifier()).isZero();

        harness.castInstant(player1, 0, supporter.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(supporter.getPowerModifier()).isEqualTo(3);
        assertThat(warbrute.getPowerModifier()).isEqualTo(6);
        assertThat(warbrute.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist may be declined without tapping the supporter")
    void canDeclineEnlist() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new CoalitionWarbrute());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(supporter.isTapped()).isFalse();
        assertThat(warbrute.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enlist excludes attacking, tapped, and opposing creatures")
    void enlistExcludesIneligibleSupporters() {
        addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent tapped = addCreatureReady(player1, new CoalitionWarbrute());
        tapped.tap();
        addCreatureReady(player2, new CoalitionWarbrute());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
    }

    @Test
    @DisplayName("The enlist power bonus expires at end of turn")
    void enlistBonusExpiresAtEndOfTurn() {
        Permanent warbrute = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new CoalitionWarbrute());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();
        assertThat(warbrute.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warbrute.getPowerModifier()).isZero();
        assertThat(warbrute.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Trample assigns excess damage from the enlist bonus to the defender")
    void enlistedPowerTramplesOverBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CoalitionWarbrute());
        Permanent supporter = addCreatureReady(player1, new CoalitionWarbrute());
        Permanent blocker = addCreatureReady(player2, new CoalitionWarbrute());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
