package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InSearchOfGreatnessTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a permanent whose mana value is one above another permanent")
    void castsMatchingPermanentFromHand() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        addCreatureReady(player1, new LlanowarElves());
        GrizzlyBears freeCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(freeCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Uses one when no other permanent is controlled")
    void excludesInSearchOfGreatnessFromManaValueCheck() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        LlanowarElves freeCard = new LlanowarElves();
        harness.setHand(player1, new ArrayList<>(List.of(freeCard)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
    }

    @Test
    @DisplayName("Scries when no eligible permanent is available")
    void scriesWhenNoPermanentMatches() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        harness.setHand(player1, new ArrayList<>(List.of(new LightningBolt())));
        setLibrary(player1, new LightningBolt());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scries after declining every eligible permanent")
    void scriesAfterDecliningAllOffers() {
        harness.addToBattlefield(player1, new InSearchOfGreatness());
        addCreatureReady(player1, new LlanowarElves());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(first, second)));
        setLibrary(player1, new LightningBolt());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    private void setLibrary(Player player, Card topCard) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).add(topCard);
    }
}
