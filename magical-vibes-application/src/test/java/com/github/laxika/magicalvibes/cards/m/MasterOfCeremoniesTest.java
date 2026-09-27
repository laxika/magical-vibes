package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfCeremonies.class, GrizzlyBears.class})
class MasterOfCeremoniesTest extends BaseCardTest {

    @Test
    void moneyMakesBothPlayersCreateTreasure() {
        beginChoice();

        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.MONEY);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Citizen")).isZero();
        assertThat(countPermanents(player2, "Citizen")).isZero();
    }

    @Test
    void friendsMakesBothPlayersCreateCitizens() {
        beginChoice();

        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.FRIENDS);

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        assertThat(countPermanents(player2, "Citizen")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void secretsMakesBothPlayersDraw() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        beginChoice();

        harness.handleListChoice(player2, ChoiceContext.MasterOfCeremoniesChoice.SECRETS);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void beginChoice() {
        harness.addToBattlefield(player1, new MasterOfCeremonies());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.MasterOfCeremoniesChoice.OPTIONS);
    }
}
