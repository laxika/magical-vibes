package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingInformant.class, BorosRecruit.class})
class LurkingInformantTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting puts the target player's top card into their graveyard")
    void acceptsPuttingTopCardIntoGraveyard() {
        addReadyInformant();
        Card topCard = new BorosRecruit();
        harness.setLibrary(player2, java.util.List.of(topCard));

        activate(player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining leaves the target player's top card on their library")
    void declinesPuttingTopCardIntoGraveyard() {
        addReadyInformant();
        Card topCard = new BorosRecruit();
        harness.setLibrary(player2, java.util.List.of(topCard));

        activate(player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("The ability can target the controller's library")
    void targetsController() {
        addReadyInformant();
        Card topCard = new BorosRecruit();
        harness.setLibrary(player1, java.util.List.of(topCard));

        activate(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An empty target library produces no may choice")
    void emptyLibrary() {
        addReadyInformant();
        gd.playerDecks.get(player2.getId()).clear();

        activate(player2.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top card is put into the graveyard")
    void leavesRemainingLibraryInOrder() {
        addReadyInformant();
        Card topCard = new BorosRecruit();
        Card secondCard = new BorosRecruit();
        Card thirdCard = new BorosRecruit();
        harness.setLibrary(player2, java.util.List.of(topCard, secondCard, thirdCard));

        activate(player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, thirdCard);
    }

    @Test
    @DisplayName("Activation pays two generic mana and taps the Informant before resolution")
    void paysActivationCostsBeforeResolving() {
        Permanent informant = addReadyInformant();
        Card topCard = new BorosRecruit();
        harness.setLibrary(player2, java.util.List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(informant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.pendingMayAbilities).isEmpty();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The activated ability still resolves after the Informant leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent informant = addReadyInformant();
        Card topCard = new BorosRecruit();
        harness.setLibrary(player2, java.util.List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(informant);
        harness.setGraveyard(player1, java.util.List.of(informant.getCard()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
    private Permanent addReadyInformant() {
        return addCreatureReady(player1, new LurkingInformant());
    }

    private void activate(UUID targetPlayerId) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, targetPlayerId);
        harness.passBothPriorities();
    }
}
