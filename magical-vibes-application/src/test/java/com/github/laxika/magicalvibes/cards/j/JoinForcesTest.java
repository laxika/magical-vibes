package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.k.KnightOfDawnsLight;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoinForces.class, KnightOfDawnsLight.class, Mountain.class})
class JoinForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures are untapped and get +2/+2")
    void untapsAndBoostsBothTargets() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();

        castJoinForces(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("One target may be chosen")
    void allowsOneTarget() {
        Permanent target = addTappedCreature();

        castJoinForces(List.of(target.getId()));

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addTappedCreature();
        castJoinForces(List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new JoinForces()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zero targets may be chosen without affecting other creatures")
    void allowsZeroTargets() {
        Permanent creature = addTappedCreature();

        castJoinForces(List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Join Forces");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Already untapped creatures of either player get the boost")
    void boostsUntappedCreaturesOfEitherPlayer() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new KnightOfDawnsLight());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new KnightOfDawnsLight());
        Permanent unchosen = addTappedCreature();

        castJoinForces(List.of(own.getId(), opposing.getId()));

        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, unchosen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unchosen)).isEqualTo(2);
    }

    @Test
    @DisplayName("The remaining legal target is affected when the first target leaves")
    void resolvesForRemainingTarget() {
        Permanent removed = addTappedCreature();
        Permanent remaining = addTappedCreature();
        harness.setHand(player1, List.of(new JoinForces()));
        addMana();
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());

        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Join Forces");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void rejectsThreeTargets() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();
        Permanent third = addTappedCreature();
        harness.setHand(player1, List.of(new JoinForces()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KnightOfDawnsLight());
        creature.tap();
        return creature;
    }

    private void castJoinForces(List<UUID> targets) {
        harness.setHand(player1, List.of(new JoinForces()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
