package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandTheChaff.class, GrizzlyBears.class, Forest.class})
class CommandTheChaffTest extends BaseCardTest {

    @Test
    @DisplayName("Offers nonland cards from the targeted opponent's sideboard")
    void castsCardFromTargetOpponentsSideboard() {
        Card chosen = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        Card casterSideboardCard = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(casterSideboardCard)));
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(chosen, remaining)));

        CommandTheChaff command = castCommandTheChaff();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(casterSideboardCard);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(remaining);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("Declining leaves the targeted sideboard unchanged and exiles Command the Chaff")
    void decliningLeavesTargetSideboardUnchanged() {
        Card sideboardCard = new GrizzlyBears();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(sideboardCard)));

        CommandTheChaff command = castCommandTheChaff();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sideboardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    @Test
    @DisplayName("Does not offer lands from the targeted sideboard")
    void doesNotOfferLandCards() {
        Forest forest = new Forest();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(forest)));

        CommandTheChaff command = castCommandTheChaff();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(command);
    }

    private CommandTheChaff castCommandTheChaff() {
        CommandTheChaff command = new CommandTheChaff();
        harness.setHand(player1, List.of(command));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        return command;
    }
}
