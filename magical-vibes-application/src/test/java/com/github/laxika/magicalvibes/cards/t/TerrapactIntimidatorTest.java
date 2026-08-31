package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerrapactIntimidator.class})
class TerrapactIntimidatorTest extends BaseCardTest {

    @Test
    void targetOpponentMayHaveControllerCreateTwoLanders() {
        castIntimidator();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Lander")).hasSize(2);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
        assertThat(findPermanent(player1, "Terrapact Intimidator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningPutsTwoPlusOnePlusOneCountersOnThisCreature() {
        castIntimidator();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanent(player1, "Terrapact Intimidator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new TerrapactIntimidator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castIntimidator() {
        harness.setHand(player1, List.of(new TerrapactIntimidator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
