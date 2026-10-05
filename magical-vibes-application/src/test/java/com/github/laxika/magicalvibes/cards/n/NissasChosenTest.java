package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.h.HideousEnd;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasChosen.class, CruelEdict.class, HideousEnd.class, IntoTheRoil.class, TurnToFrog.class})
class NissasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("When Nissa's Chosen would die, it is put on the bottom of its owner's library instead")
    void putOnBottomOfLibraryInsteadOfDying() {
        Card filler = new CruelEdict();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player1, new NissasChosen());

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.assertNotInGraveyard(player1, "Nissa's Chosen");

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(0)).isSameAs(filler);
        assertThat(library.get(1).getName()).isEqualTo("Nissa's Chosen");
    }

    @Test
    void destructionPutsChosenAtBottomOfLibrary() {
        Card filler = new NissasChosen();
        NissasChosen chosen = new NissasChosen();
        harness.setLibrary(player1, List.of(filler));
        var permanent = harness.addToBattlefieldAndReturn(player1, chosen);
        harness.setHand(player2, List.of(new HideousEnd()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.assertNotInGraveyard(player1, "Nissa's Chosen");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler, chosen);
        harness.assertLife(player1, 18);
    }

    @Test
    void returningToHandDoesNotApplyDeathReplacement() {
        NissasChosen chosen = new NissasChosen();
        harness.setLibrary(player1, List.of());
        var permanent = harness.addToBattlefieldAndReturn(player1, chosen);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.assertInHand(player1, "Nissa's Chosen");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void losingAbilitiesDisablesDeathReplacement() {
        harness.setLibrary(player1, List.of());
        var permanent = harness.addToBattlefieldAndReturn(player1, new NissasChosen());
        harness.setHand(player2, List.of(new TurnToFrog(), new HideousEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, permanent.getId());
        harness.castAndResolveInstant(player2, 0, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.assertInGraveyard(player1, "Nissa's Chosen");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
