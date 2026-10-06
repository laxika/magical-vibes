package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlurSliver;
import com.github.laxika.magicalvibes.cards.b.BrindleBoar;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDiplomats.class, BrindleBoar.class, BlurSliver.class, Mutavault.class, WallOfSwords.class})
class GoblinDiplomatsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping forces every creature, on both sides, to attack this turn")
    void forcesAllCreaturesToAttack() {
        Permanent diplomats = addCreatureReady(player1, new GoblinDiplomats());
        Permanent ownBoar = addCreatureReady(player1, new BrindleBoar());
        Permanent enemyBoar = addCreatureReady(player2, new BrindleBoar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diplomats.isTapped()).isTrue();
        assertThat(diplomats.isMustAttackThisTurn()).isTrue();
        assertThat(ownBoar.isMustAttackThisTurn()).isTrue();
        assertThat(enemyBoar.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The must-attack requirement wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GoblinDiplomats());
        Permanent enemyBoar = addCreatureReady(player2, new BrindleBoar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(enemyBoar.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void requirementStartsOnlyWhenAbilityResolves() {
        Permanent diplomats = addCreatureReady(player1, new GoblinDiplomats());
        Permanent boar = addCreatureReady(player1, new BrindleBoar());

        harness.activateAbility(player1, 0, null, null);

        assertThat(diplomats.isTapped()).isTrue();
        assertThat(boar.isMustAttackThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(boar.isMustAttackThisTurn()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GoblinDiplomats());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotOmitAnAbleCreatureFromAttackDeclaration() {
        addCreatureReady(player1, new GoblinDiplomats());
        addCreatureReady(player1, new BrindleBoar());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatCode(() -> declareAttackers(List.of(1))).doesNotThrowAnyException();
    }

    @Test
    void tappedSummoningSickAndDefenderCreaturesNeedNotAttack() {
        addCreatureReady(player1, new GoblinDiplomats());
        Permanent tappedBoar = addCreatureReady(player1, new BrindleBoar());
        tappedBoar.tap();
        harness.addToBattlefield(player1, new BrindleBoar());
        addCreatureReady(player1, new WallOfSwords());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void creatureEnteringAfterResolutionWithHasteMustAttack() {
        addCreatureReady(player1, new GoblinDiplomats());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new BlurSliver(), "{2}{R}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blur Sliver");

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatCode(() -> declareAttackers(List.of(1))).doesNotThrowAnyException();
    }

    @Test
    void permanentBecomingCreatureAfterResolutionMustAttack() {
        addCreatureReady(player1, new GoblinDiplomats());
        addCreatureReady(player1, new Mutavault());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatCode(() -> declareAttackers(List.of(1))).doesNotThrowAnyException();
    }
}
