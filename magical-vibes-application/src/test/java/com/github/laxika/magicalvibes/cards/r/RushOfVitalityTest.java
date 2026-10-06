package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushOfVitality.class, GrizzlyBears.class, FountainOfYouth.class, Terror.class})
class RushOfVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0, lifelink, and indestructible")
    void grantsBoostLifelinkAndIndestructible() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bear);

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bear);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RushOfVitality()));
        addMana();

        UUID targetId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void lifelinkBenefitsTheOpposingCreaturesController() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castResolve(bear);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }

    @Test
    void survivesLethalCombatDamageAndGainsLifeForFullDamageDealt() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castResolve(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, List.of(0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    void indestructiblePreventsDestroyEffects() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(bear);
        harness.setHand(player1, List.of(new Terror()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Terror");
    }

    @Test
    void doesNotAffectAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RushOfVitality()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rush of Vitality");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(other.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new RushOfVitality()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
