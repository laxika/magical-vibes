package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.i.Island;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnollspineDragon.class, Island.class, FlameJavelin.class})
class KnollspineDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting discards the hand and draws cards equal to damage dealt to target opponent")
    void discardsHandAndDrawsEqualToDamage() {
        damagePlayer(player2.getId());
        damagePlayer(player2.getId()); // 8 damage dealt to player2 this turn
        harness.setLibrary(player1, List.of(
                new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));

        castDragon(List.of(new Island(), new Island()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Discarded 2 cards, then drew 8 (the damage total).
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(8)
                .allMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Only opponents are offered as valid targets")
    void targetFilterExcludesController() {
        damagePlayer(player2.getId());
        harness.setLibrary(player1, List.of(new Island()));

        castDragon(List.of(new Island()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Declining leaves the hand untouched and draws nothing")
    void decliningDoesNothing() {
        damagePlayer(player2.getId()); // 4 damage
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castDragon(List.of(new Island(), new Island()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Island"));
        harness.assertNotInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Accepting with no damage dealt still discards the hand but draws nothing")
    void noDamageDiscardsButDrawsNothing() {
        // player2 took no damage this turn.
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castDragon(List.of(new Island(), new Island()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Damage dealt to the controller does not determine the draw count")
    void damageToControllerDoesNotCount() {
        damagePlayer(player1.getId());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castDragon(List.of(new Island(), new Island()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    private void castDragon(List<Card> extraHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new KnollspineDragon());
        hand.addAll(extraHandCards);
        harness.setHand(player1, hand);
        addManaForCast(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void damagePlayer(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addManaForCast(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.RED, 2);
    }

}
