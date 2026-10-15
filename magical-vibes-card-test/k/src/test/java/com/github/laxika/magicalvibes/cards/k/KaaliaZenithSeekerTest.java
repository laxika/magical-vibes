package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShadowbornDemon;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaaliaZenithSeeker.class, BaneslayerAngel.class, GrizzlyBears.class,
        ShadowbornDemon.class, ShivanDragon.class, Shock.class, UniversalAutomaton.class})
class KaaliaZenithSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Offers at most one Angel, Demon, and Dragon from the top six")
    void offersOneOfEachCreatureSubtype() {
        Card angel = new BaneslayerAngel();
        Card secondAngel = new BaneslayerAngel();
        Card demon = new ShadowbornDemon();
        Card dragon = new ShivanDragon();
        resolveKaalia(angel, demon, dragon, secondAngel, new GrizzlyBears(), new Shock());

        assertThat(currentSearch().params().cards()).containsExactly(angel, secondAngel);

        choose(angel);
        assertThat(currentSearch().params().cards()).containsExactly(demon);

        choose(demon);
        assertThat(currentSearch().params().cards()).containsExactly(dragon);

        choose(dragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, demon, dragon);
    }

    @Test
    @DisplayName("Skips missing subtypes and puts the unchosen cards on the bottom randomly")
    void skipsMissingSubtypeAndFinishesWithoutReorderPrompt() {
        Card angel = new BaneslayerAngel();
        Card dragon = new ShivanDragon();
        resolveKaalia(angel, dragon, new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());

        choose(angel);
        assertThat(currentSearch().params().cards()).containsExactly(dragon);

        choose(dragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, dragon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Each subtype may be declined even when eligible cards exist")
    void mayDeclineEverySubtype() {
        Card angel = new BaneslayerAngel();
        Card demon = new ShadowbornDemon();
        Card dragon = new ShivanDragon();
        resolveKaalia(angel, demon, dragon);

        harness.handleCardChosen(player1, -1);
        assertThat(currentSearch().params().cards()).containsExactly(demon);
        harness.handleCardChosen(player1, -1);
        assertThat(currentSearch().params().cards()).containsExactly(dragon);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(angel, demon, dragon);
    }

    @Test
    @DisplayName("A short library can yield a Dragon without an Angel or Demon")
    void shortLibraryWithOnlyDragonCategory() {
        Card dragon = new ShivanDragon();
        Card shock = new Shock();
        resolveKaalia(dragon, shock);

        assertThat(currentSearch().params().cards()).containsExactly(dragon);
        choose(dragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Only the top six cards are inspected and the untouched library stays above the rest")
    void doesNotLookBelowTopSix() {
        List<Card> topSix = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        Card seventh = new ShivanDragon();
        resolveKaalia(topSix.get(0), topSix.get(1), topSix.get(2), topSix.get(3),
                topSix.get(4), topSix.get(5), seventh);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSix);
    }

    @Test
    @DisplayName("An empty library finishes the trigger without a choice")
    void emptyLibrary() {
        resolveKaalia();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Changeling cards can fill all three categories, with each card taken only once")
    void changelingsAreEligibleForEveryCategory() {
        Card first = new UniversalAutomaton();
        Card second = new UniversalAutomaton();
        Card third = new UniversalAutomaton();
        resolveKaalia(first, second, third);

        assertThat(currentSearch()).isNotNull();
        assertThat(currentSearch().params().cards()).containsExactly(first, second, third);
        choose(first);
        assertThat(currentSearch().params().cards()).containsExactly(second, third);
        choose(second);
        assertThat(currentSearch().params().cards()).containsExactly(third);
        choose(third);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A changeling remains eligible for the Demon category after selecting a printed Angel")
    void changelingEligibleForLaterCategory() {
        Card angel = new BaneslayerAngel();
        Card changeling = new UniversalAutomaton();
        resolveKaalia(angel, changeling);

        choose(angel);
        assertThat(currentSearch()).isNotNull();
        assertThat(currentSearch().params().cards()).containsExactly(changeling);
        choose(changeling);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(angel, changeling);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveKaalia(Card... topCards) {
        harness.setHand(player1, List.of(new KaaliaZenithSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(topCards));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch currentSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void choose(Card chosenCard) {
        PendingInteraction.LibrarySearch search = currentSearch();
        int index = 0;
        while (!search.params().cards().get(index).getId().equals(chosenCard.getId())) {
            index++;
        }
        harness.handleCardChosen(player1, index);
    }
}
