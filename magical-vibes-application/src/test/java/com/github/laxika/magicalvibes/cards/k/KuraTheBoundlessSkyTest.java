package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KuraTheBoundlessSky.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class KuraTheBoundlessSkyTest extends BaseCardTest {

    private static final String SEARCH_MODE =
            "Search your library for up to three land cards, reveal them, and put them into your hand";
    private static final String TOKEN_MODE =
            "Create an X/X green Spirit creature token, where X is the number of lands you control";

    @Test
    @DisplayName("The search mode puts up to three land cards from the library into your hand")
    void searchModeFindsUpToThreeLands() {
        Forest forest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, mountain, bears));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island, mountain);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("The token mode creates a Spirit whose power and toughness equal your land count")
    void tokenModeUsesControlledLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
    }

    private void killKura() {
        Permanent kura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KuraTheBoundlessSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kura));
        harness.passBothPriorities();
    }
}
