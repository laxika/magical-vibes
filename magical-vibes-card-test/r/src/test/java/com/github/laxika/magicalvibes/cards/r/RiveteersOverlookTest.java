package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JetmirsGarden;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiveteersOverlook.class, Forest.class, CivicGardener.class, Mountain.class, Swamp.class,
        Island.class, Plains.class, JetmirsGarden.class})
class RiveteersOverlookTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Riveteers Overlook before the search trigger resolves")
    void enteringSacrificesIt() {
        RiveteersOverlook overlook = new RiveteersOverlook();
        harness.setHand(player1, List.of(overlook));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(overlook.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(overlook.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(overlook.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Swamp, Mountain, or Forest and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playOverlook();
        Card swamp = new Swamp();
        Card mountain = new Mountain();
        Card forest = new Forest();
        setLibrary(swamp, mountain, forest, new CivicGardener(), new Island(), new Plains(), new JetmirsGarden());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(swamp, mountain, forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playOverlook();
        Card swamp = new Swamp();
        setLibrary(swamp, new Mountain(), new Forest());

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(swamp.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playOverlook();
        setLibrary(new CivicGardener());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    void sacrificeQueuesSeparateSearchTrigger() {
        harness.setLife(player1, 20);
        setLibrary(new Swamp());
        playOverlook();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riveteers Overlook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void failingToFindStillGainsLife() {
        harness.setLife(player1, 20);
        Card swamp = new Swamp();
        setLibrary(swamp);
        playOverlook();
        resolveToSearchPrompt();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillGainsLife() {
        harness.setLife(player1, 20);
        setLibrary();
        playOverlook();

        resolveToSearchPrompt();

        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sourceLeavingBeforeTriggerResolvesPreventsSearchAndLifeGain() {
        harness.setLife(player1, 20);
        setLibrary(new Swamp());
        playOverlook();
        Card overlook = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(overlook);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    private void playOverlook() {
        harness.setHand(player1, List.of(new RiveteersOverlook()));
        harness.playLand(player1, 0);
    }

    private void resolveToSearchPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
