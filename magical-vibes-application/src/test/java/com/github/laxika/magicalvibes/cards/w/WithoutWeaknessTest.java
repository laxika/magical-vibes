package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.l.LethalSting;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WithoutWeakness.class, FeralProwler.class, LethalSting.class, OpenFire.class})
class WithoutWeaknessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants indestructible to target creature you control")
    void grantsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FeralProwler());

        castResolve(bears);

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FeralProwler());

        castResolve(bears);
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        // A controlled creature exists (spell is playable), but the opponent's creature is illegal.
        harness.addToBattlefield(player1, new FeralProwler());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new WithoutWeakness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID opponentId = opponent.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cycling pays two generic mana and discards before drawing on resolution")
    void cyclingDiscardsThenDraws() {
        harness.setHand(player1, List.of(new WithoutWeakness()));
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Without Weakness");
        harness.assertNotInHand(player1, "Without Weakness");
        harness.assertNotInHand(player1, "Feral Prowler");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Feral Prowler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible prevents destruction by lethal damage")
    void survivesLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        castResolve(creature);
        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Feral Prowler");
        harness.assertNotInGraveyard(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("Indestructible prevents a destroy spell")
    void survivesDestroySpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        Permanent counterBearer = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        castResolve(creature);
        harness.setHand(player1, List.of(new LethalSting()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), counterBearer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertNotInGraveyard(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("Indestructible does not prevent death from zero toughness")
    void diesWithZeroToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        castResolve(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Feral Prowler");
        harness.assertInGraveyard(player1, "Feral Prowler");
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new WithoutWeakness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
