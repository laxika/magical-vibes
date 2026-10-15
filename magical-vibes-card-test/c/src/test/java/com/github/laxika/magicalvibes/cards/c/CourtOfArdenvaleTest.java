package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfArdenvale.class, GrizzlyBears.class, AirElemental.class, LightningBolt.class})
class CourtOfArdenvaleTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfArdenvale());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("As the monarch, returns a target small permanent card to the battlefield")
    void monarchReturnsPermanentToBattlefield() {
        Card target = new GrizzlyBears();
        harness.enterBattlefieldAndReturn(player1, new CourtOfArdenvale());
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("When not the monarch, returns a target small permanent card to hand")
    void nonMonarchReturnsPermanentToHand() {
        Card target = new GrizzlyBears();
        harness.enterBattlefieldAndReturn(player1, new CourtOfArdenvale());
        resolveAllTriggers();
        gd.monarchPlayerId = player2.getId();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only permanent cards with mana value 3 or less are legal targets")
    void filtersGraveyardTargets() {
        Card legal = new GrizzlyBears();
        Card expensive = new AirElemental();
        Card nonPermanent = new LightningBolt();
        harness.enterBattlefieldAndReturn(player1, new CourtOfArdenvale());
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of(legal, expensive, nonPermanent));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());
    }
}
