package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThievesFortuneTest extends BaseCardTest {

    private Card[] stackFourOnTop() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Shock();
        Card top4 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4)); // top1 is the very top
        return new Card[]{top1, top2, top3, top4};
    }

    @Test
    @DisplayName("Normal cast looks at top four; chosen card to hand, rest reordered onto the bottom")
    void normalCastPicksOneToHand() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ThievesFortune()));
        harness.addMana(player1, ManaColor.BLUE, 3); // normal {2}{U}

        Card[] top = stackFourOnTop();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId()));

        // Chosen card is in hand.
        assertThat(gd.playerHands.get(player1.getId())).contains(top[0]);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top[1], top[2], top[3]);

        // The other three must be ordered onto the bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(top[1], top[2], top[3]);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(reorder.indexOf(top[1]), reorder.indexOf(top[2]), reorder.indexOf(top[3]))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // First chosen is closest to the top of the bottom section.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[1], top[2], top[3]);
        harness.assertInGraveyard(player1, "Thieves' Fortune");
    }

    @Test
    @DisplayName("Prowl cast (Rogue dealt combat damage) works for its cheaper cost")
    void prowlCastPicksOneToHand() {
        setupProwl();
        harness.setHand(player1, List.of(new ThievesFortune()));
        harness.addMana(player1, ManaColor.BLUE, 1); // prowl {U}

        Card[] top = stackFourOnTop();

        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[1]);
        harness.assertInGraveyard(player1, "Thieves' Fortune");
    }

    @Test
    @DisplayName("Prowl cost is unavailable without combat damage from a Rogue this turn")
    void prowlUnavailableWithoutRogueDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ThievesFortune()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupProwl() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ROGUE);
    }
}
