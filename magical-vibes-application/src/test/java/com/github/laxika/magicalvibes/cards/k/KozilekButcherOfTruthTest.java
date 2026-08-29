package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KozilekButcherOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kozilek draws four cards")
    void castingDrawsFourCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KozilekButcherOfTruth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking with Kozilek makes the defending player sacrifice four permanents")
    void annihilatorFour() {
        Permanent kozilek = addCreatureReady(player1, new KozilekButcherOfTruth());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kozilek)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Kozilek goes to a graveyard, its owner's graveyard is shuffled into their library")
    void shufflesItsOwnersGraveyardIntoLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent kozilek = addCreatureReady(player1, new KozilekButcherOfTruth());
        kozilek.setMarkedDamage(13);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactlyInAnyOrder("Kozilek, Butcher of Truth", "Grizzly Bears");
    }
}
