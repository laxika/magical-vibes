package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GuacMarshmallowPizza.class, Forest.class, GrizzlyBears.class})
class GuacMarshmallowPizzaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield boosts and untaps the targeted creature")
    void enteringBattlefieldBoostsAndUntapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new GuacMarshmallowPizza()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enters-the-battlefield ability can target only a creature")
    void enteringBattlefieldCannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GuacMarshmallowPizza()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target");
    }

    @Test
    @DisplayName("Sacrificing it gains 3 life")
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new GuacMarshmallowPizza());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Guac & Marshmallow Pizza");
        harness.assertInGraveyard(player1, "Guac & Marshmallow Pizza");
    }

    @Test
    @DisplayName("The creature boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuacMarshmallowPizza()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice is paid before the life gain resolves")
    void sacrificeIsPaidBeforeLifeGainResolves() {
        Permanent pizza = harness.addToBattlefieldAndReturn(player1, new GuacMarshmallowPizza());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Guac & Marshmallow Pizza");
        harness.assertInGraveyard(player1, "Guac & Marshmallow Pizza");
        harness.assertLife(player1, 20);
        assertThat(pizza.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A tapped Pizza cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent pizza = harness.addToBattlefieldAndReturn(player1, new GuacMarshmallowPizza());
        pizza.tap();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Guac & Marshmallow Pizza");
        harness.assertNotInGraveyard(player1, "Guac & Marshmallow Pizza");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Food ability requires two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent pizza = harness.addToBattlefieldAndReturn(player1, new GuacMarshmallowPizza());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Guac & Marshmallow Pizza");
        harness.assertNotInGraveyard(player1, "Guac & Marshmallow Pizza");
        harness.assertLife(player1, 20);
        assertThat(pizza.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during combat")
    void canBeCastDuringCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new GuacMarshmallowPizza()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guac & Marshmallow Pizza");
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enter trigger resolves even after Pizza is sacrificed")
    void enterTriggerResolvesAfterSourceIsSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new GuacMarshmallowPizza()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Guac & Marshmallow Pizza");
        harness.assertLife(player1, 23);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }
}
