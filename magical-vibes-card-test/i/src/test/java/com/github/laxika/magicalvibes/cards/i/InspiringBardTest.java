package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiringBard.class, Island.class})
class InspiringBardTest extends BaseCardTest {

    @Test
    void bardicInspirationBoostsTargetCreatureUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new InspiringBard());

        cast(0, bear);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    void songOfRestGainsThreeLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);

        cast(1, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 7);
    }

    @Test
    void bardicInspirationCannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(0, island))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bardicInspirationCanTargetTheBardThatJustEntered() {
        cast(0, null);
        Permanent bard = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, bard.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bard)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bard)).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new InspiringBard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, choice.options().get(mode));
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
        }
    }
}
