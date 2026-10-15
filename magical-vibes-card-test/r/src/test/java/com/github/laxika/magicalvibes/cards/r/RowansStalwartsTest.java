package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RowansStalwarts.class, RowanFearlessSparkmage.class})
class RowansStalwartsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability returns Rowan from the graveyard")
    void findsRowanInGraveyard() {
        Card rowan = new RowanFearlessSparkmage();
        harness.setGraveyard(player1, List.of(rowan));

        castStalwarts();
        resolveMay(true);

        harness.assertInHand(player1, "Rowan, Fearless Sparkmage");
        harness.assertNotInGraveyard(player1, "Rowan, Fearless Sparkmage");
    }

    @Test
    @DisplayName("Accepting the ETB ability searches the library for Rowan")
    void findsRowanInLibrary() {
        Card rowan = new RowanFearlessSparkmage();
        harness.setLibrary(player1, List.of(rowan));

        castStalwarts();
        resolveMay(true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(rowan);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability does not search for Rowan")
    void declinesSearch() {
        Card rowan = new RowanFearlessSparkmage();
        harness.setGraveyard(player1, List.of(rowan));

        castStalwarts();
        resolveMay(false);

        harness.assertInGraveyard(player1, "Rowan, Fearless Sparkmage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castStalwarts() {
        harness.setHand(player1, List.of(new RowansStalwarts()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }
}
