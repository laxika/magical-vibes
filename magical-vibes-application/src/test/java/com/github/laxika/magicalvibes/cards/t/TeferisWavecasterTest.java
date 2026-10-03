package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisWavecaster.class, TeferiTimelessVoyager.class})
class TeferisWavecasterTest extends BaseCardTest {

    @Test
    void acceptsOptionalSearchFromGraveyard() {
        Card teferi = new TeferiTimelessVoyager();
        harness.setGraveyard(player1, List.of(teferi));
        castWavecaster();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Teferi, Timeless Voyager");
        harness.assertNotInGraveyard(player1, "Teferi, Timeless Voyager");
    }

    @Test
    void acceptsOptionalSearchFromLibrary() {
        Card teferi = new TeferiTimelessVoyager();
        harness.setLibrary(player1, List.of(teferi));
        castWavecaster();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Teferi, Timeless Voyager");
    }

    @Test
    void maySearchCanBeDeclined() {
        Card teferi = new TeferiTimelessVoyager();
        harness.setGraveyard(player1, List.of(teferi));
        castWavecaster();

        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Teferi, Timeless Voyager");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castWavecaster() {
        harness.setHand(player1, List.of(new TeferisWavecaster()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
