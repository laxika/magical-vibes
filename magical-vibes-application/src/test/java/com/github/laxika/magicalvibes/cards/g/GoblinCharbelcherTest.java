package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

class GoblinCharbelcherTest extends BaseCardTest {

    private void activateAt(UUID targetId, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new GoblinCharbelcher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    private void finishLibraryOrder() {
        List<Card> revealed = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(IntStream.range(0, revealed.size()).boxed().toList()));
    }

    @Test
    void dealsNonlandCountDamageUntilLand() {
        activateAt(player2.getId(), List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        finishLibraryOrder();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void doublesDamageWhenTheRevealedLandIsMountain() {
        activateAt(player2.getId(), List.of(new GrizzlyBears(), new GrizzlyBears(), new Mountain()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        finishLibraryOrder();
    }

    @Test
    void dealsDamageForEveryRevealedCardWhenLibraryHasNoLand() {
        activateAt(player2.getId(), List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        finishLibraryOrder();
    }
}
