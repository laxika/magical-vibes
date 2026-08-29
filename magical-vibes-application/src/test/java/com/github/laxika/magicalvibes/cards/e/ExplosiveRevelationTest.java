package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExplosiveRevelationTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToFirstNonlandManaValueAndPutsItIntoHand() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsRevealedLandsOnBottomInChosenOrder() {
        Card forest = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, bears));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
