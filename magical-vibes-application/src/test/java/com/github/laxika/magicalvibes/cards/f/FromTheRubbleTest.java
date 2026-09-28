package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromTheRubble.class, GrizzlyBears.class, GoblinPiker.class})
class FromTheRubbleTest extends BaseCardTest {

    @Test
    void choosesCreatureTypeWhenItEnters() {
        harness.setHand(player1, List.of(new FromTheRubble()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Permanent rubble = findPermanent(player1, "From the Rubble");
        assertThat(rubble.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
    }

    @Test
    void returnsChosenCreatureTypeWithFinalityCounterAtEndStep() {
        harness.setHand(player1, List.of(new FromTheRubble()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Card bear = new GrizzlyBears();
        Card goblin = new GoblinPiker();
        harness.setGraveyard(player1, List.of(goblin, bear));

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
