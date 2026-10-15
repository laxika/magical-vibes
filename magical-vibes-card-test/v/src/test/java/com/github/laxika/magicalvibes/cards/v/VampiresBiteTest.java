package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiresBite.class, GrizzlyBears.class, FountainOfYouth.class, Disfigure.class})
class VampiresBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, gives the target creature +3/+0")
    void resolvesWithoutKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("With kicker, gives the target creature +3/+0 and lifelink")
    void resolvesWithKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The boost and lifelink wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @CsvSource({"false, false", "true, false", "false, true", "true, true"})
    @DisplayName("The boosted creature deals damage and only gains life when kicked, for its controller")
    void combatDamageAndLifelink(boolean kicked, boolean opposingCreature) {
        Player controller = opposingCreature ? player2 : player1;
        Player defender = opposingCreature ? player1 : player2;
        Permanent bear = addCreatureReady(controller, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, kicked ? 2 : 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.castKickedInstant(player1, 0, bear.getId());
        } else {
            harness.castInstant(player1, 0, bear.getId());
        }
        harness.passBothPriorities();

        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        bear.setAttacking(true);
        bear.setAttackTarget(defender.getId());
        harness.resolveCombatDamage();

        harness.assertLife(defender, 15);
        harness.assertLife(controller, kicked ? 25 : 20);
    }

    @Test
    @DisplayName("Kicking requires paying the additional cost")
    void cannotKickWithOnlyBaseCostMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Vampire's Bite");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A kicked spell does not affect a replacement creature after its target dies")
    void removedTargetDoesNotTransferEffects() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresBite()));
        harness.setHand(player2, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castKickedInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire's Bite");
        assertThat(replacement.getPowerModifier()).isZero();
        assertThat(replacement.getToughnessModifier()).isZero();
        assertThat(replacement.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
