package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallFromFavor.class, GrizzlyBears.class})
class FallFromFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, taps the enchanted creature, and makes its controller the monarch")
    void entersTapsAndMakesControllerMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFallFromFavor(player1, creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Keeps the enchanted creature tapped while its controller is not the monarch")
    void locksEnchantedCreatureWhileItsControllerIsNotMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castFallFromFavor(player1, creature);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows the enchanted creature to untap when its controller becomes the monarch")
    void allowsUntapWhenEnchantedControllerIsMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castFallFromFavor(player1, creature);
        gd.monarchPlayerId = player2.getId();
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    private void castFallFromFavor(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new FallFromFavor()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castEnchantment(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
