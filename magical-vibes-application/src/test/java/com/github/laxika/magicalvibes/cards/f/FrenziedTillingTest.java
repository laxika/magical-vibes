package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.q.QuirionSentinel;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrenziedTilling.class, Island.class, Mountain.class, CoastalTower.class, QuirionSentinel.class})
class FrenziedTillingTest extends BaseCardTest {

    // "Destroy target land. Search your library for a basic land card, put that card onto the
    //  battlefield tapped, then shuffle."

    private void giveFrenziedTilling() {
        harness.setHand(player1, List.of(new FrenziedTilling()));
        harness.addMana(player1, ManaColor.RED, 4); // {3}{R} paid with red
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }

    @Test
    @DisplayName("Destroys the target land and fetches a basic land onto the battlefield tapped")
    void destroysLandAndFetchesBasic() {
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Mountain(), new CoastalTower(), new QuirionSentinel()));
        giveFrenziedTilling();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Island"));
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Island")).isZero();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(card -> card.getName().equals("Coastal Tower"));
        harness.handleCardChosen(player1, 0);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Failing to find still leaves the land destroyed")
    void failToFind() {
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Mountain()));
        giveFrenziedTilling();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Island"));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(countPermanents(player2, "Island")).isZero();
        assertThat(countPermanents(player1, "Mountain")).isZero();
    }

    @Test
    @DisplayName("With no basic land in the library there is no search prompt")
    void noBasicLandNoPrompt() {
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new QuirionSentinel()));
        giveFrenziedTilling();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Island"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player2, "Island")).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new QuirionSentinel());
        giveFrenziedTilling();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Quirion Sentinel")))
                .isInstanceOf(IllegalStateException.class);
    }
}
