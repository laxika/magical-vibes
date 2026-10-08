package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.t.TouchTheSpiritRealm;
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

@CardUsed({WhenWeWereYoung.class, JukaiTrainee.class, AutomatedArtificer.class, TouchTheSpiritRealm.class})
class WhenWeWereYoungTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures each get +2/+2")
    void boostsBothTargets() {
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player2, new JukaiTrainee());

        castWhenWeWereYoung(List.of(first.getId(), second.getId()));

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Grants lifelink when the controller has an artifact and an enchantment")
    void grantsLifelinkWithArtifactAndEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addToBattlefieldAndReturn(player1, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not grant lifelink without both an artifact and an enchantment")
    void doesNotGrantLifelinkWithoutBothPermanentTypes() {
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The boost and lifelink expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addToBattlefieldAndReturn(player1, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TouchTheSpiritRealm());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithNoTargets() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "When We Were Young");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void grantsLifelinkToBothTargetsIncludingOpponentsCreature() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player2, new JukaiTrainee());
        Permanent untargeted = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(first.getId(), second.getId()));

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        assertThat(untargeted.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void artifactAloneDoesNotGrantLifelink() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void enchantmentAloneDoesNotGrantLifelink() {
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentsEnchantmentDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        harness.addToBattlefield(player2, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void grantsLifelinkIfConditionBecomesTrueBeforeResolution() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotGrantLifelinkIfConditionBecomesFalseBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void lifelinkPersistsAfterArtifactLeavesFollowingResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        castWhenWeWereYoung(List.of(target.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void resolvesForRemainingTargetWhenOtherTargetLeaves() {
        harness.addToBattlefield(player1, new AutomatedArtificer());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player2, new JukaiTrainee());
        prepareCast();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, first));
        harness.passBothPriorities();

        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "When We Were Young");
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player1, new JukaiTrainee());
        Permanent third = addCreatureReady(player2, new JukaiTrainee());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWhenWeWereYoung(List<java.util.UUID> targets) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WhenWeWereYoung()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
