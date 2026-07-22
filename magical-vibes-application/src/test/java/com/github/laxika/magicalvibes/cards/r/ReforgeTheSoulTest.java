package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReforgeTheSoulTest extends BaseCardTest {

    private void fillLibraries(int cardsEach) {
        List.of(player1, player2).forEach(p -> {
            List<com.github.laxika.magicalvibes.model.Card> deck = new ArrayList<>();
            for (int i = 0; i < cardsEach; i++) {
                deck.add(new GrizzlyBears());
            }
            harness.setLibrary(p, deck);
        });
    }

    @Test
    @DisplayName("Each player discards their hand and draws seven cards")
    void discardsHandsAndDrawsSeven() {
        fillLibraries(10);
        harness.setHand(player1, List.of(new ReforgeTheSoul(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reforge the Soul"));
    }

    @Test
    @DisplayName("Drawing as the first card this turn offers a miracle reveal")
    void firstDrawOffersMiracleReveal() {
        ReforgeTheSoul reforge = new ReforgeTheSoul();
        harness.setLibrary(player1, List.of(reforge));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(reforge.getId()));
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new ReforgeTheSoul()));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Miracle cast for {1}{R} resolves the wheel")
    void miracleCastResolvesWheel() {
        fillLibraries(10);
        ReforgeTheSoul reforge = new ReforgeTheSoul();
        List<com.github.laxika.magicalvibes.model.Card> p1Lib = new ArrayList<>();
        p1Lib.add(reforge);
        for (int i = 0; i < 10; i++) {
            p1Lib.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, p1Lib);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.passBothPriorities(); // miracle trigger → cast prompt
        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
