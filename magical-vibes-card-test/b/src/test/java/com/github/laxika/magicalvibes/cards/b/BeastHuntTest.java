package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastHunt.class, GrizzlyBears.class, Shock.class, Forest.class, Island.class})
class BeastHuntTest extends BaseCardTest {

    @Test
    void putsRevealedCreaturesIntoHandAndTheRestIntoGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(bears, shock, forest));

        harness.setHand(player1, List.of(new BeastHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest);
    }

    @Test
    void putsAllRevealedNoncreaturesIntoGraveyard() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card island = new Island();

        harness.setLibrary(player1, List.of(shock, forest, island));

        harness.setHand(player1, List.of(new BeastHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest, island);
    }

    @Test
    void revealsAllAvailableCardsWhenLibraryHasFewerThanThree() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();

        harness.setLibrary(player1, List.of(bears, shock));

        harness.setHand(player1, List.of(new BeastHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void putsAllThreeCreaturesIntoHandAndLeavesFourthCardInLibrary() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new BeastHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawingOrLosing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BeastHunt()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Beast Hunt");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
