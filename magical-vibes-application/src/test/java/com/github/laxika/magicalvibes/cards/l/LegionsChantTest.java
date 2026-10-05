package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionsChant.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Plains.class})
class LegionsChantTest extends BaseCardTest {

    @Test
    @DisplayName("Returns any number of own creature cards within its starting intensity")
    void returnsCreaturesWithinIntensity() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card giant = new HillGiant();
        Card plains = new Plains();
        Card opponentGiant = new HillGiant();
        LegionsChant chant = new LegionsChant();
        harness.setGraveyard(player1, List.of(bears, elves, giant, plains));
        harness.setGraveyard(player2, List.of(opponentGiant));
        harness.setLibrary(player1, List.of(new LegionsChant()));
        harness.castFromHand(player1, chant, "{2}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        assertThat(gd.getCardIntensity(chant.getId())).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(bears, elves);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(giant, plains, chant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGiant);
    }

    @Test
    @DisplayName("All owned Chorus cards intensify after the return resolves")
    void intensifiesOwnedChorusCards() {
        LegionsChant chant = new LegionsChant();
        LegionsChant otherChant = new LegionsChant();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(otherChant));
        harness.castFromHand(player1, chant, "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(chant.getId())).isEqualTo(4);
        assertThat(gd.getCardIntensity(otherChant.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("May return no creatures and still intensify owned Chorus cards")
    void mayChooseNoCreatures() {
        Card bears = new GrizzlyBears();
        LegionsChant chant = new LegionsChant();
        LegionsChant opponentChant = new LegionsChant();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(opponentChant));
        harness.setLibrary(player1, List.of(new LegionsChant()));

        harness.castFromHand(player1, chant, "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(bears, chant);
        assertThat(gd.getCardIntensity(chant)).isEqualTo(4);
        assertThat(gd.getCardIntensity(opponentChant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The combined mana value of the selected creatures cannot exceed intensity")
    void rejectsSelectionOverCombinedLimit() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears, secondBears));
        harness.setLibrary(player1, List.of(new LegionsChant()));

        harness.castFromHand(player1, new LegionsChant(), "{2}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(firstBears, secondBears);

        harness.handleMultipleCardsChosen(player1, List.of(firstBears.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(firstBears);
    }

    @Test
    @DisplayName("An uncast copy intensified by another Chant uses starting intensity plus the increase")
    void previouslyIntensifiedCopyReturnsFourManaCreature() {
        LegionsChant firstChant = new LegionsChant();
        LegionsChant secondChant = new LegionsChant();
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(secondChant));
        harness.setGraveyard(player1, List.of(giant));

        harness.castFromHand(player1, firstChant, "{2}{W}");
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new LegionsChant()));
        harness.castFromHand(player1, secondChant, "{2}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(giant);
        assertThat(gd.getCardIntensity(secondChant)).isEqualTo(5);
    }
}
