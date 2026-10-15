package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BlightbellyRat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmbulatoryEdifice.class, BlightbellyRat.class, Forest.class})
class AmbulatoryEdificeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life gives a target creature -1/-1 until end of turn")
    void payingLifeShrinksTargetUntilEndOfTurn() {
        Permanent rat = harness.addToBattlefieldAndReturn(player2, new BlightbellyRat());
        castAmbulatoryEdifice();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.handlePermanentChosen(player1, rat.getId());
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the life payment does not give the target -1/-1")
    void decliningLifePaymentDoesNothing() {
        Permanent rat = harness.addToBattlefieldAndReturn(player2, new BlightbellyRat());
        castAmbulatoryEdifice();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    @DisplayName("The reflexive trigger cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAmbulatoryEdifice();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 18);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With less than two life the payment cannot create the reflexive trigger")
    void cannotPayWithInsufficientLife() {
        harness.setLife(player1, 1);
        castAmbulatoryEdifice();
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

    }

    private void castAmbulatoryEdifice() {
        harness.setHand(player1, List.of(new AmbulatoryEdifice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
