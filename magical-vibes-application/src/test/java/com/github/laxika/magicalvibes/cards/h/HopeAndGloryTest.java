package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.s.Scrapheap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HopeAndGlory.class, AngelicCurator.class, Scrapheap.class})
class HopeAndGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts both target creatures")
    void untapsAndBoostsBothTargets() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();
        castHopeAndGlory(first, second);

        assertThat(first.isTapped()).isFalse();
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.isTapped()).isFalse();
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void canTargetOpponentControlledCreature() {
        Permanent own = addTappedCreature();
        Permanent opponent = addTappedCreature(player2);
        castHopeAndGlory(own, opponent);

        assertThat(own.isTapped()).isFalse();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);
        assertThat(opponent.isTapped()).isFalse();
        assertThat(opponent.getPowerModifier()).isEqualTo(1);
        assertThat(opponent.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The +1/+1 bonuses expire at end of turn")
    void bonusesExpireAtEndOfTurn() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();
        castHopeAndGlory(first, second);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresExactlyTwoCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        harness.setHand(player1, List.of(new HopeAndGlory()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Scrapheap());
        harness.setHand(player1, List.of(new HopeAndGlory()));
        addMana();

        List<UUID> targets = List.of(creature.getId(), artifact.getId());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        harness.setHand(player1, List.of(new HopeAndGlory()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillResolvesForRemainingLegalTarget() {
        Permanent remaining = addTappedCreature();
        Permanent removed = addTappedCreature();
        castHopeAndGloryWithoutResolving(remaining, removed);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isFalse();
        assertThat(remaining.getPowerModifier()).isEqualTo(1);
        assertThat(remaining.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addTappedCreature() {
        return addTappedCreature(player1);
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new AngelicCurator());
        creature.tap();
        return creature;
    }

    private void castHopeAndGlory(Permanent first, Permanent second) {
        prepareHopeAndGlory();
        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
    }

    private void castHopeAndGloryWithoutResolving(Permanent first, Permanent second) {
        prepareHopeAndGlory();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
    }

    private void prepareHopeAndGlory() {
        harness.setHand(player1, List.of(new HopeAndGlory()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
