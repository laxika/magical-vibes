package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshlingTheLimitless.class, AirElemental.class})
class AshlingTheLimitlessTest extends BaseCardTest {

    @Test
    void grantsEvokeAndCopiesTheSacrificedElementalWithHaste() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Air Elemental")).hasSize(1);
        Permanent token = findPermanents(player1, "Air Elemental").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(SacrificePermanentAtControllerEndStepUnlessPays.class))
                .hasSize(1);
    }

    @Test
    void tokenIsSacrificedAtItsControllersNextEndStepUnlessPaidFor() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Air Elemental")).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
