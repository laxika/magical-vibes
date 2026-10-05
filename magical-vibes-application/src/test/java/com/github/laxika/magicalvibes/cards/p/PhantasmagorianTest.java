package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Phantasmagorian.class, GossamerPhantasm.class})
class PhantasmagorianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield when no player discards three cards")
    void entersWhenNoPlayerDiscardsThreeCards() {
        castPhantasmagorian(player1, List.of(), List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmagorian");
    }

    @Test
    @DisplayName("Any player may discard three cards to counter it")
    void anyPlayerMayDiscardThreeCardsToCounterIt() {
        castPhantasmagorian(player1, List.of(), threeGossamerPhantasms());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        discardThreeCards(player2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantasmagorian");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Remaining players still receive the discard choice after one player accepts")
    void remainingPlayersStillReceiveTheChoice() {
        castPhantasmagorian(player1, threeGossamerPhantasms(), threeGossamerPhantasms());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        discardThreeCards(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantasmagorian");
    }

    @Test
    @DisplayName("Discarding three cards returns it from the graveyard to its owner's hand")
    void returnsFromGraveyardToHand() {
        Phantasmagorian phantasmagorian = new Phantasmagorian();
        harness.setGraveyard(player1, List.of(phantasmagorian));
        harness.setHand(player1, threeGossamerPhantasms());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        discardThreeCards(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phantasmagorian");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Phantasmagorian"));
    }

    @Test
    @DisplayName("The graveyard ability requires no mana beyond discarding three cards")
    void graveyardAbilityRequiresNoMana() {
        Phantasmagorian phantasmagorian = new Phantasmagorian();
        harness.setGraveyard(player1, List.of(phantasmagorian));
        harness.setHand(player1, threeGossamerPhantasms());

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    void spellResolvesWhenBothEligiblePlayersDecline() {
        castPhantasmagorian(player1, threeGossamerPhantasms(), threeGossamerPhantasms());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmagorian");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    void playerWithOnlyTwoCardsCannotDiscardToCounterSpell() {
        castPhantasmagorian(player1, List.of(), List.of(new GossamerPhantasm(), new GossamerPhantasm()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmagorian");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void graveyardAbilityCannotBeActivatedWithOnlyTwoCards() {
        Phantasmagorian source = new Phantasmagorian();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(new GossamerPhantasm(), new GossamerPhantasm()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityReturnsOnlyItsSourceCopyAndPaysCostBeforeResolution() {
        Phantasmagorian source = new Phantasmagorian();
        Phantasmagorian other = new Phantasmagorian();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, threeGossamerPhantasms());

        harness.activateGraveyardAbility(player1, 0);
        discardThreeCards(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source, other).hasSize(5);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(source).hasSize(4);
    }

    @Test
    void olderActivationCannotReturnSourceAfterItLeavesAndReentersGraveyard() {
        Phantasmagorian source = new Phantasmagorian();
        Phantasmagorian other = new Phantasmagorian();
        harness.setGraveyard(player1, List.of(source, other));
        List<Card> hand = new ArrayList<>(threeGossamerPhantasms());
        hand.addAll(threeGossamerPhantasms());
        hand.add(new GossamerPhantasm());
        hand.add(new GossamerPhantasm());
        harness.setHand(player1, hand);

        harness.activateGraveyardAbility(player1, 0);
        discardThreeCards(player1);
        harness.activateGraveyardAbility(player1, 0);
        discardThreeCards(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(source).hasSize(3);

        harness.activateGraveyardAbility(player1, 0);
        discardThreeCards(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    private void castPhantasmagorian(Player caster, List<Card> casterCards, List<Card> opponentCards) {
        harness.setHand(player2, opponentCards);
        harness.castFromHand(caster, new Phantasmagorian(), "{5}{B}{B}");
        harness.setHand(caster, casterCards);
    }

    private void discardThreeCards(Player player) {
        harness.handleCardChosen(player, 0);
        harness.handleCardChosen(player, 0);
        harness.handleCardChosen(player, 0);
    }

    private List<Card> threeGossamerPhantasms() {
        return List.of(new GossamerPhantasm(), new GossamerPhantasm(), new GossamerPhantasm());
    }

}
