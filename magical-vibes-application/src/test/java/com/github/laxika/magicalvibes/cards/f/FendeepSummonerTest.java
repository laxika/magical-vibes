package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FendeepSummoner.class, Swamp.class, GrizzlyBears.class})
class FendeepSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Animates two target Swamps into 3/5 Treefolk Warriors that are still lands")
    void animatesTwoSwamps() {
        Permanent summoner = addCreatureReady(player1, new FendeepSummoner());
        Permanent swampA = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent swampB = harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swampA.getId(), swampB.getId()));
        harness.passBothPriorities();

        assertThat(summoner.isTapped()).isTrue();
        for (Permanent swamp : List.of(swampA, swampB)) {
            assertThat(swamp.isAnimatedUntilEndOfTurn()).isTrue();
            assertThat(gqs.isCreature(gd, swamp)).isTrue();
            assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(5);
            assertThat(gqs.effectiveCreatureSubtypes(gd, swamp))
                    .contains(CardSubtype.TREEFOLK, CardSubtype.WARRIOR);
            // Types are additive — the Swamp is still a land.
            assertThat(gqs.isLand(gd, swamp)).isTrue();
        }
    }

    @Test
    @DisplayName("Up to two — a single target is legal")
    void animatesSingleSwamp() {
        addCreatureReady(player1, new FendeepSummoner());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, swamp)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(5);
    }

    @Test
    @DisplayName("Up to two - no targets is legal")
    void activatesWithoutTargets() {
        Permanent summoner = addCreatureReady(player1, new FendeepSummoner());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(summoner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a Swamp controlled by an opponent")
    void animatesOpponentsSwamp() {
        addCreatureReady(player1, new FendeepSummoner());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, swamp)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(5);
        assertThat(gqs.isLand(gd, swamp)).isTrue();
    }

    @Test
    @DisplayName("Cannot choose more than two target Swamps")
    void cannotTargetMoreThanTwoSwamps() {
        addCreatureReady(player1, new FendeepSummoner());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Swamp());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addCreatureReady(player1, new FendeepSummoner());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(swamp.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, swamp))
                .doesNotContain(CardSubtype.TREEFOLK, CardSubtype.WARRIOR);
        assertThat(gqs.isLand(gd, swamp)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a Swamp")
    void cannotTargetNonSwamp() {
        addCreatureReady(player1, new FendeepSummoner());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

}
