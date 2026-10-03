package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianConjurer.class, SnowCoveredPlains.class, AdarkarWastes.class, BalduvianBears.class,
        SwordsToPlowshares.class})
class BalduvianConjurerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting the snow land")
    void activatingPutsOnStack() {
        Permanent conjurer = addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        harness.activateAbility(player1, 0, null, snowLand.getId());

        assertThat(conjurer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(snowLand.getId());
    }

    @Test
    @DisplayName("Resolving animates target snow land into a 2/2 creature that is still a land")
    void animatesSnowLandIntoCreature() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(snowLand.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
    }

    @Test
    @DisplayName("Can animate an opponent's snow land")
    void animatesOpponentsSnowLand() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(snowLand.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    @Test
    @DisplayName("Does not animate a target that is no longer snow when the ability resolves")
    void targetMustStillBeSnowOnResolution() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        harness.activateAbility(player1, 0, null, snowLand.getId());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent nonsnowLand = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonsnowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addCreatureReady(player1, new BalduvianConjurer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Conjurer cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Conjurer cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent conjurer = addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        conjurer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Animation persists after the Conjurer leaves the battlefield")
    void animationPersistsWithoutConjurer() {
        Permanent conjurer = addCreatureReady(player1, new BalduvianConjurer());
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, conjurer.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(conjurer);
        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
    }
}
