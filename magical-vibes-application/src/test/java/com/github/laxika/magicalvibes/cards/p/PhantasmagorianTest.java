package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Phantasmagorian.class, GrizzlyBears.class})
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
        castPhantasmagorian(player1, List.of(), threeBears());

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
        castPhantasmagorian(player1, threeBears(), threeBears());

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
        harness.setHand(player1, threeBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        discardThreeCards(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phantasmagorian");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Phantasmagorian"));
    }

    private void castPhantasmagorian(Player caster, List<Card> casterCards, List<Card> opponentCards) {
        Phantasmagorian phantasmagorian = new Phantasmagorian();
        harness.setHand(caster, concat(List.of(phantasmagorian), casterCards));
        harness.setHand(player2, opponentCards);
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 5);
        harness.castCreature(caster, 0);
    }

    private void discardThreeCards(Player player) {
        harness.handleCardChosen(player, 0);
        harness.handleCardChosen(player, 0);
        harness.handleCardChosen(player, 0);
    }

    private List<Card> threeBears() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }

    private List<Card> concat(List<Card> first, List<Card> second) {
        List<Card> cards = new ArrayList<>(first);
        cards.addAll(second);
        return cards;
    }
}
