package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TribalUnity.class, GrizzlyBears.class, GiantSpider.class})
class TribalUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature type gets +X/+X on every battlefield")
    void boostsChosenTypeByPaidX() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownSpider = addCreatureReady(player1, new GiantSpider());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TribalUnity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSpider)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TribalUnity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
