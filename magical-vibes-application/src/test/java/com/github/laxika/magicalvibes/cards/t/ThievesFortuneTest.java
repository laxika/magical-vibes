package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThievesFortune.class, ElvishWarrior.class, IndomitableAncients.class,
        MudbuttonClanger.class, MothdustChangeling.class})
class ThievesFortuneTest extends BaseCardTest {

    private Card[] stackFourOnTop() {
        Card top1 = new ElvishWarrior();
        Card top2 = new IndomitableAncients();
        Card top3 = new MudbuttonClanger();
        Card top4 = new MothdustChangeling();
        Card untouched = new ElvishWarrior();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4, untouched)); // top1 is the very top
        return new Card[]{top1, top2, top3, top4, untouched};
    }

    @Test
    @DisplayName("Normal cast looks at top four; chosen card to hand, rest reordered onto the bottom")
    void normalCastPicksOneToHand() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        Card[] top = stackFourOnTop();

        harness.castFromHand(player1, new ThievesFortune(), "{2}{U}");
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
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[4], top[1], top[2], top[3]);
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

        // CR 608.2n: the spell reaches the graveyard only as the final part of its resolution, so the
        // "rest on the bottom in any order" step has to be answered before it leaves the stack.
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.indexOf(top[0]), reorder.indexOf(top[2]), reorder.indexOf(top[3]))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[4], top[0], top[2], top[3]);
        harness.assertInGraveyard(player1, "Thieves' Fortune");
    }

    @Test
    @DisplayName("With only one library card, it goes to hand without another choice")
    void worksWithOneCardLibrary() {
        Card onlyCard = new ElvishWarrior();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new ThievesFortune(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Thieves' Fortune");
    }

    @Test
    @DisplayName("With an empty library, it resolves without a choice")
    void worksWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new ThievesFortune(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
