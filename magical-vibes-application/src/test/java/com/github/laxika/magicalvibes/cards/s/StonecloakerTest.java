package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WhitemaneLion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stonecloaker.class, WhitemaneLion.class})
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
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB returns a creature and exiles a targeted card from any graveyard")
    void etbReturnsCreatureAndExilesGraveyardCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WhitemaneLion());
        Card graveyardCard = new WhitemaneLion();
        harness.setGraveyard(player2, List.of(graveyardCard));
        Stonecloaker stonecloaker = new Stonecloaker();
        harness.setHand(player1, List.of(stonecloaker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveStonecloakerTriggers(creature.getId(), graveyardCard.getId());

        harness.assertInHand(player1, "Whitemane Lion");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
    }

    @Test
    @DisplayName("ETB can exile a targeted card from its controller's graveyard")
    void etbCanExileCardFromControllerGraveyard() {
        Card graveyardCard = new WhitemaneLion();
        harness.setGraveyard(player1, List.of(graveyardCard));
        Stonecloaker stonecloaker = new Stonecloaker();
        harness.setHand(player1, List.of(stonecloaker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        UUID stonecloakerId = harness.getPermanentId(player1, "Stonecloaker");
        resolveStonecloakerTriggers(stonecloakerId, graveyardCard.getId());

        harness.assertInHand(player1, "Stonecloaker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
    }

    @Test
    @DisplayName("ETB requires choosing a graveyard target when one is available")
    void etbRequiresGraveyardTarget() {
        Card graveyardCard = new WhitemaneLion();
        harness.setGraveyard(player2, List.of(graveyardCard));
        Stonecloaker stonecloaker = new Stonecloaker();
        harness.setHand(player1, List.of(stonecloaker));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            UUID stonecloakerId = harness.getPermanentId(player1, "Stonecloaker");
            harness.handlePermanentChosen(player1, stonecloakerId);
            resolveAllTriggers();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
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
