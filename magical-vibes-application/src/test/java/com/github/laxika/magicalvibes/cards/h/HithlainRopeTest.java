package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HithlainRope.class, Forest.class, GrizzlyBears.class})
class HithlainRopeTest extends BaseCardTest {

    @Test
    @DisplayName("The search ability gets a tapped basic land and passes the Rope to the right")
    void searchesForTappedBasicLandThenPassesRope() {
        Permanent rope = addRope(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .singleElement()
                .extracting(card -> card.hasType(CardType.LAND))
                .isEqualTo(true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rope);
    }

    @Test
    @DisplayName("The draw ability draws a card and passes the Rope to the right")
    void drawsCardThenPassesRope() {
        Permanent rope = addRope(player1);
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rope);
    }

    @Test
    @DisplayName("The Rope cannot be sacrificed")
    void cannotBeSacrificed() {
        Permanent rope = addRope(player1);

        assertThat(gqs.cantBeSacrificed(gd, rope)).isTrue();
    }

    private Permanent addRope(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player, new HithlainRope());
    }

    @Test
    @DisplayName("Failing to find a basic land still passes the Rope")
    void failingToFindStillPassesRope() {
        Permanent rope = addRope(player1);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rope);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rope);
        assertThat(rope.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library still passes the Rope")
    void emptyLibraryStillPassesRope() {
        Permanent rope = addRope(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rope);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rope);
        assertThat(rope.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The new controller draws for themselves and passes the Rope back")
    void newControllerCanDrawAndPassRopeBack() {
        Permanent rope = addRope(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.performUntapStep(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rope);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rope);
        assertThat(gqs.cantBeSacrificed(gd, rope)).isTrue();
    }
}
