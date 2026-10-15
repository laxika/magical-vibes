package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BurglarRat;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.p.PitilessGorgon;
import com.github.laxika.magicalvibes.cards.t.TorchCourier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MausoleumSecrets.class, DeadWeight.class, BurglarRat.class, HuntedWitness.class,
        TorchCourier.class, MaximizeVelocity.class, PitilessGorgon.class})
class MausoleumSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a revealed black card up to the creature count in the graveyard")
    void searchesForBlackCardWithinCreatureCount() {
        harness.setGraveyard(player1, List.of(new HuntedWitness(), new HuntedWitness()));
        Card deadWeight = new DeadWeight();
        Card burglarRat = new BurglarRat();
        harness.setLibrary(player1, List.of(deadWeight, burglarRat, new TorchCourier(), new MaximizeVelocity()));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(deadWeight, burglarRat);

        harness.handleCardChosen(player1, search.params().cards().indexOf(burglarRat));

        assertThat(gd.playerHands.get(player1.getId())).contains(burglarRat);
    }

    @Test
    @DisplayName("Excludes black cards above the creature count and nonblack cards")
    void appliesCreatureCountAndBlackFilters() {
        harness.setGraveyard(player1, List.of(new HuntedWitness()));
        Card deadWeight = new DeadWeight();
        Card burglarRat = new BurglarRat();
        Card goblin = new TorchCourier();
        harness.setLibrary(player1, List.of(deadWeight, burglarRat, goblin));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(deadWeight);
    }

    @Test
    @DisplayName("Finds no eligible positive-mana-value cards with no creatures in the graveyard")
    void noEligibleCardsWithNoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new MaximizeVelocity()));
        harness.setLibrary(player1, List.of(new DeadWeight()));

        castSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void countsOnlyControllersCreatureCards() {
        harness.setGraveyard(player1, List.of(new HuntedWitness(), new DeadWeight(), new MaximizeVelocity()));
        harness.setGraveyard(player2, List.of(new HuntedWitness(), new HuntedWitness()));
        Card deadWeight = new DeadWeight();
        harness.setLibrary(player1, List.of(deadWeight, new BurglarRat(), new PitilessGorgon()));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(deadWeight);
    }

    @Test
    void canFindMulticoloredBlackCardAtManaValueLimit() {
        harness.setGraveyard(player1, List.of(new HuntedWitness(), new HuntedWitness(), new HuntedWitness()));
        Card gorgon = new PitilessGorgon();
        harness.setLibrary(player1, List.of(gorgon, new TorchCourier()));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(gorgon);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(gorgon);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(gorgon).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canFailToFindEvenWhenAnEligibleCardExists() {
        harness.setGraveyard(player1, List.of(new HuntedWitness()));
        Card deadWeight = new DeadWeight();
        harness.setLibrary(player1, List.of(deadWeight));

        castSpell();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deadWeight);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void evaluatesCreatureCountWhenSpellResolves() {
        harness.setGraveyard(player1, List.of(new HuntedWitness()));
        Card rat = new BurglarRat();
        harness.setLibrary(player1, List.of(rat));
        harness.setHand(player1, List.of(new MausoleumSecrets()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        harness.setGraveyard(player1, List.of(new HuntedWitness(), new HuntedWitness()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(rat);
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new MausoleumSecrets()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
