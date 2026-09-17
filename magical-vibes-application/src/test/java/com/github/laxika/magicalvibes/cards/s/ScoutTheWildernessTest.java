package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoutTheWilderness.class, Forest.class, GrizzlyBears.class})
class ScoutTheWildernessTest extends BaseCardTest {

    @Test
    void searchesForABasicLandAndPutsItOntoTheBattlefieldTapped() {
        Forest forest = new Forest();
        Card otherCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of(forest, otherCard));
        addBaseMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    void kickedCastAlsoCreatesTwoSoldierTokens() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ScoutTheWilderness()));
        harness.setLibrary(player1, List.of(forest));
        addKickedMana();

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allMatch(permanent -> permanent.getCard().isToken());
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
