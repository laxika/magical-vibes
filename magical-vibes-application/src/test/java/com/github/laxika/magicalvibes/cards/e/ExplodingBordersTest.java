package com.github.laxika.magicalvibes.cards.e;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplodingBordersTest extends BaseCardTest {

    // "Domain — Search your library for a basic land card, put that card onto the battlefield
    //  tapped, then shuffle. Exploding Borders deals X damage to target player or planeswalker,
    //  where X is the number of basic land types among lands you control."

    private void giveExplodingBorders() {
        harness.setHand(player1, List.of(new ExplodingBorders()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{R} paid with red
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }

    private void setLibrary(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    @Test
    @DisplayName("Fetched basic land enters tapped and counts toward the domain damage")
    void fetchedLandCountsTowardDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        setLibrary(List.of(new Mountain(), new GrizzlyBears()));
        giveExplodingBorders();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Search pauses resolution; only the basic Mountain is offered, put onto battlefield tapped.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .findFirst().orElseThrow();
        assertThat(mountain.isTapped()).isTrue();

        // Forest + Island + fetched Mountain = 3 basic land types = 3 damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
    }

    @Test
    @DisplayName("Failing to find still deals damage for the currently controlled basic land types")
    void failToFindStillDealsDamage() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        setLibrary(List.of(new Forest()));
        giveExplodingBorders();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1)); // fail to find

        // Only Swamp + Mountain = 2 basic land types = 2 damage.
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 2);
    }

    @Test
    @DisplayName("With no basic lands controlled or found, deals no damage")
    void noBasicLandTypesDealsNoDamage() {
        setLibrary(List.of(new GrizzlyBears()));
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
}
