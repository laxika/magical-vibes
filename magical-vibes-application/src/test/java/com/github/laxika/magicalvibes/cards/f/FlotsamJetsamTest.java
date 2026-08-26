package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlotsamJetsam.class, CounselOfTheSoratami.class, Island.class})
class FlotsamJetsamTest extends BaseCardTest {

    @Test
    @DisplayName("Flotsam mills three cards and creates a Clue")
    void flotsamMillsAndInvestigates() {
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first, second, third)
                .hasSize(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Jetsam mills each opponent and casts one opponent-graveyard spell for free")
    void jetsamMillsAndCastsFromOpponentGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setGraveyard(player2, List.of(counsel));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FlotsamJetsam()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.findExiledCard(counsel.getId()).card()).isSameAs(counsel);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
