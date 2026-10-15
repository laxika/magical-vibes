package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ExplodingBorders.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Swamp.class, NicolBolasPlaneswalker.class})
class ExplodingBordersTest extends BaseCardTest {

    // "Domain — Search your library for a basic land card, put that card onto the battlefield
    //  tapped, then shuffle. Exploding Borders deals X damage to target player or planeswalker,
    //  where X is the number of basic land types among lands you control."

    private void giveExplodingBorders() {
        harness.setHand(player1, List.of(new ExplodingBorders()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{R} paid with red
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }

    @Test
    @DisplayName("Fetched basic land enters tapped and counts toward the domain damage")
    void fetchedLandCountsTowardDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));
        giveExplodingBorders();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Search pauses resolution; only the basic Mountain is offered, put onto battlefield tapped.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        harness.handleCardChosen(player1, 0);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain.isTapped()).isTrue();

        // Forest + Island + fetched Mountain = 3 basic land types = 3 damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
    }

    @Test
    @DisplayName("Failing to find still deals damage for the currently controlled basic land types")
    void failToFindStillDealsDamage() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest()));
        giveExplodingBorders();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1); // fail to find

        // Only Swamp + Mountain = 2 basic land types = 2 damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 2);
    }

    @Test
    @DisplayName("With no basic lands controlled or found, deals no damage")
    void noBasicLandTypesDealsNoDamage() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        giveExplodingBorders();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull(); // no basic land to search, no prompt
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveExplodingBorders();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated land types count once and opponents' lands do not count")
    void duplicateAndOpposingLandTypesDoNotIncreaseDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.setLibrary(player1, List.of(new Forest()));
        giveExplodingBorders();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Can target its controller and still search that controller's library")
    void canTargetController() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Mountain()));
        giveExplodingBorders();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An empty library does not stop domain damage")
    void emptyLibraryStillDealsDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of());
        giveExplodingBorders();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Domain damage removes loyalty from a targeted planeswalker")
    void canDamagePlaneswalker() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new NicolBolasPlaneswalker());
        Permanent bolas = findPermanent(player2, "Nicol Bolas, Planeswalker");
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Mountain()));
        giveExplodingBorders();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, bolas.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("If the sole planeswalker target leaves, neither search nor damage happens")
    void illegalSoleTargetPreventsSearch() {
        harness.addToBattlefield(player2, new NicolBolasPlaneswalker());
        Permanent bolas = findPermanent(player2, "Nicol Bolas, Planeswalker");
        harness.setLibrary(player1, List.of(new Mountain()));
        giveExplodingBorders();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, bolas.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bolas);
        gd.playerGraveyards.get(player2.getId()).add(bolas.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
