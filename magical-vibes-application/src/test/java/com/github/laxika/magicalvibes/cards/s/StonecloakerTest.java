package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stonecloaker.class, GrizzlyBears.class})
class StonecloakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can return Stonecloaker itself to its owner's hand")
    void etbCanReturnItself() {
        Stonecloaker stonecloaker = new Stonecloaker();
        harness.setHand(player1, List.of(stonecloaker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        UUID stonecloakerId = harness.getPermanentId(player1, "Stonecloaker");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(stonecloakerId);
        harness.handlePermanentChosen(player1, stonecloakerId);

        harness.assertInHand(player1, "Stonecloaker");
    }

    @Test
    @DisplayName("ETB returns a creature and exiles a targeted card from any graveyard")
    void etbReturnsCreatureAndExilesGraveyardCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        Stonecloaker stonecloaker = new Stonecloaker();
        harness.setHand(player1, List.of(stonecloaker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveStonecloakerTriggers(creature.getId(), graveyardCard.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
    }

    private void resolveStonecloakerTriggers(UUID creatureToReturnId, UUID graveyardCardId) {
        while (!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) {
            Object interaction = gd.interaction.activeInteraction();
            if (interaction instanceof PendingInteraction.MultiGraveyardChoice) {
                harness.handleMultipleCardsChosen(player1, List.of(graveyardCardId));
            } else if (interaction instanceof PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, creatureToReturnId);
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
