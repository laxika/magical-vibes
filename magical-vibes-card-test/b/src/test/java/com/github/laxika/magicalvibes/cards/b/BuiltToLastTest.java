package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AppetiteForTheUnnatural;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
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

@CardUsed({BuiltToLast.class, GrizzlyBears.class, Ornithopter.class,
        BastionMastodon.class, AppetiteForTheUnnatural.class, PropheticPrism.class})
class BuiltToLastTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an artifact creature and grants it indestructible")
    void boostsArtifactCreatureAndGrantsIndestructible() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castResolve(ornithopter);

        assertThat(ornithopter.getPowerModifier()).isEqualTo(2);
        assertThat(ornithopter.getToughnessModifier()).isEqualTo(2);
        assertThat(ornithopter.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Boosts a nonartifact creature without granting indestructible")
    void boostsNonartifactCreatureWithoutIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears);

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castResolve(ornithopter);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ornithopter.getPowerModifier()).isZero();
        assertThat(ornithopter.getToughnessModifier()).isZero();
        assertThat(ornithopter.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canBoostOpponentsArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BastionMastodon());

        castResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void indestructiblePreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        castResolve(target);
        harness.setHand(player2, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player1, "Bastion Mastodon");
        harness.assertNotInGraveyard(player1, "Bastion Mastodon");
    }

    @Test
    void removedTargetDoesNotReceiveEitherEffect() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        harness.setHand(player1, List.of(new BuiltToLast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bastion Mastodon");
        harness.assertInGraveyard(player1, "Built to Last");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new BuiltToLast()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new BuiltToLast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
