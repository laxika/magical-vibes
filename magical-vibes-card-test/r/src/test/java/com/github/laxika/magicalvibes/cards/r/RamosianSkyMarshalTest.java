package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AerialCaravan;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.s.SoothingBalm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamosianSkyMarshal.class, RappellingScouts.class, JhovallQueen.class,
        AerialCaravan.class, SoothingBalm.class})
class RamosianSkyMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 6 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadySkyMarshal();
        harness.setLibrary(player1, List.of(
                new RamosianSkyMarshal(),
                new RappellingScouts(),
                new JhovallQueen(),
                new AerialCaravan(),
                new SoothingBalm()));

        activateSkyMarshal();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Ramosian Sky Marshal", "Rappelling Scouts", "Jhovall Queen");
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadySkyMarshal();
        harness.setLibrary(player1, List.of(new RappellingScouts()));

        activateSkyMarshal();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sky Marshal", "Rappelling Scouts");
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadySkyMarshal();
        harness.setLibrary(player1, List.of(new AerialCaravan(), new SoothingBalm()));

        activateSkyMarshal();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Sky Marshal");
    }

    @Test
    @DisplayName("Activating the ability pays seven generic mana and taps Ramosian Sky Marshal")
    void activationPaysManaAndTapsSkyMarshal() {
        Permanent skyMarshal = addReadySkyMarshal();
        harness.setLibrary(player1, List.of());

        activateSkyMarshal();

        assertThat(skyMarshal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A Rebel with mana value exactly six enters untapped under your control")
    void findsRebelAtManaValueLimit() {
        addReadySkyMarshal();
        JhovallQueen queen = new JhovallQueen();
        SoothingBalm remainingCard = new SoothingBalm();
        harness.setLibrary(player1, List.of(queen, remainingCard));
        harness.setLibrary(player2, List.of(new RappellingScouts()));

        activateSkyMarshal();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Jhovall Queen");
        harness.assertNotOnBattlefield(player2, "Jhovall Queen");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(queen.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Rappelling Scouts");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("You may fail to find even when a matching Rebel is in the library")
    void mayDeclineMatchingRebel() {
        addReadySkyMarshal();
        RappellingScouts scouts = new RappellingScouts();
        harness.setLibrary(player1, List.of(scouts));

        activateSkyMarshal();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Rappelling Scouts");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scouts);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addReadySkyMarshal() {
        Permanent skyMarshal = addCreatureReady(player1, new RamosianSkyMarshal());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        return skyMarshal;
    }

    private void activateSkyMarshal() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
