package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VergeRangers.class, Forest.class})
class VergeRangersTest extends BaseCardTest {

    @Test
    @DisplayName("Allows playing a land from the top when an opponent controls more lands")
    void playsLandFromLibraryTopWhenOpponentHasMoreLands() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not allow playing a land from the top when no opponent controls more lands")
    void doesNotPlayLandFromLibraryTopWithoutLandAdvantage() {
        harness.addToBattlefield(player1, new VergeRangers());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }

    @Test
    void topCardIsPrivateEvenWithoutAnOpponentLandAdvantage() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void rangersOnTopOfLibraryDoNotRevealThemselves() {
        harness.setLibrary(player1, List.of(new VergeRangers()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .allMatch(message -> !message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void doesNotGrantAnExtraLandPlay() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void equalLandCountsDoNotPermitPlayingFromLibrary() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doesNotPermitCastingNonlandsFromLibrary() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        VergeRangers top = new VergeRangers();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doesNotPermitLandPlayDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new VergeRangers());
        harness.addToBattlefield(player2, new Forest());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
