package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.XathridNecromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurvivorsBond.class, XathridNecromancer.class, GrizzlyBears.class})
class SurvivorsBondTest extends BaseCardTest {

    @Test
    @DisplayName("Human mode returns only a Human creature card")
    void humanModeReturnsHumanCreature() {
        Card human = new XathridNecromancer();
        Card nonHuman = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(human, nonHuman));
        castSurvivorsBond(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(human.getId());
        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Xathrid Necromancer");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-Human mode returns only a non-Human creature card")
    void nonHumanModeReturnsNonHumanCreature() {
        Card human = new XathridNecromancer();
        Card nonHuman = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(human, nonHuman));
        castSurvivorsBond(1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(nonHuman.getId());
        harness.handleMultipleCardsChosen(player1, List.of(nonHuman.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Xathrid Necromancer");
    }

    @Test
    @DisplayName("Both mode independently targets one Human and one non-Human creature")
    void bothModeReturnsBothCreatureCategories() {
        Card human = new XathridNecromancer();
        Card nonHuman = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(human, nonHuman));
        castSurvivorsBond(2);

        PendingInteraction.MultiGraveyardChoice humanChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(humanChoice.validCardIds()).containsExactly(human.getId());
        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));

        PendingInteraction.MultiGraveyardChoice nonHumanChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(nonHumanChoice.validCardIds()).containsExactly(nonHuman.getId());
        harness.handleMultipleCardsChosen(player1, List.of(nonHuman.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Xathrid Necromancer");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castSurvivorsBond(int mode) {
        harness.setHand(player1, List.of(new SurvivorsBond()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, mode);
    }
}
