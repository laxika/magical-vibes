package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnollspineDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting discards the hand and draws cards equal to damage dealt to target opponent")
    void discardsHandAndDrawsEqualToDamage() {
        shockPlayer(player2.getId());
        shockPlayer(player2.getId()); // 4 damage dealt to player2 this turn
        setDeck(player1, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));

        castDragon(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.handleMayAbilityChosen(player1, true);   // accept -> target choice
        harness.handlePermanentChosen(player1, player2.getId());

        // Discarded 2 cards, then drew 4 (the damage total).
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(4)
                .allMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Only opponents are offered as valid targets")
    void targetFilterExcludesController() {
        shockPlayer(player2.getId());
        setDeck(player1, List.of(new Island()));

        castDragon(List.of(new GrizzlyBears()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Declining leaves the hand untouched and draws nothing")
    void decliningDoesNothing() {
        shockPlayer(player2.getId()); // 2 damage
        setDeck(player1, List.of(new Island(), new Island()));

        castDragon(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Accepting with no damage dealt still discards the hand but draws nothing")
    void noDamageDiscardsButDrawsNothing() {
        // player2 took no damage this turn.
        setDeck(player1, List.of(new Island(), new Island()));

        castDragon(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    private void castDragon(List<Card> extraHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new KnollspineDragon());
        hand.addAll(extraHandCards);
        harness.setHand(player1, hand);
        addManaForCast(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
    }

    private void shockPlayer(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new Shock())));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addManaForCast(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.RED, 2);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
