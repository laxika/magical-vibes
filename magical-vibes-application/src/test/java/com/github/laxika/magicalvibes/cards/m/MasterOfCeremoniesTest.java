package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfCeremonies.class, Forest.class})
class MasterOfCeremoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Money makes the controller and opponent create Treasures")
    void money() {
        beginUpkeep();

        assertThat(activeChoice().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.MONEY);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Friends makes the controller and opponent create Citizens")
    void friends() {
        beginUpkeep();

        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.FRIENDS);

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        assertThat(countPermanents(player2, "Citizen")).isEqualTo(1);
    }

    @Test
    @DisplayName("Secrets makes the controller and opponent draw")
    void secrets() {
        Forest controllerCard = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        beginUpkeep();

        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.SECRETS);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1)
                .contains(controllerCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1)
                .contains(opponentCard);
    }

    private void beginUpkeep() {
        harness.addToBattlefield(player1, new MasterOfCeremonies());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice activeChoice() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.MasterOfCeremoniesChoice.OPTIONS);
        return choice;
    }
}
