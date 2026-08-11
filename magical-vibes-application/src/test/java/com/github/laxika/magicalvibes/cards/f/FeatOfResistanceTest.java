package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target and grants protection from the chosen color")
    void putsCounterAndGrantsProtection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeatOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn while the counter remains")
    void protectionWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeatOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeatOfResistance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = bears.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
