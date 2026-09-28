package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfDeath.class, Forest.class, GrizzlyBears.class})
class MasterOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, surveils 2")
    void surveilsTwoWhenItEnters() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new MasterOfDeath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("During your upkeep, you may pay 1 life to return it from your graveyard to your hand")
    void paysLifeToReturnFromGraveyard() {
        MasterOfDeath master = new MasterOfDeath();
        harness.setGraveyard(player1, List.of(master));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Master of Death");
        harness.assertNotInGraveyard(player1, "Master of Death");
    }

    @Test
    @DisplayName("Declining the upkeep ability keeps it in the graveyard")
    void decliningKeepsItInGraveyard() {
        MasterOfDeath master = new MasterOfDeath();
        harness.setGraveyard(player1, List.of(master));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Master of Death");
        harness.assertNotInHand(player1, "Master of Death");
    }
}
