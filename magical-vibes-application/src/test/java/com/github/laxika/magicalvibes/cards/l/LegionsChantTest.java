package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(chant));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
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
        harness.setHand(player1, List.of(chant));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(chant.getId())).isEqualTo(4);
        assertThat(gd.getCardIntensity(otherChant.getId())).isEqualTo(1);
    }
}
