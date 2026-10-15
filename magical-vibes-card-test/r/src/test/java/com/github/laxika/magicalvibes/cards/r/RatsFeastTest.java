package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenevolentBodyguard.class, BorderPatrol.class, RatsFeast.class, SuntailHawk.class})
class RatsFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X target cards from one graveyard")
    void exilesExactlyXCardsFromOneGraveyard() {
        Card first = new SuntailHawk();
        Card second = new BenevolentBodyguard();
        Card untouched = new SuntailHawk();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player1, List.of(first, second, untouched));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched, spell);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Can exile exactly X target cards from an opponent's graveyard")
    void exilesExactlyXCardsFromOpponentsGraveyard() {
        Card first = new SuntailHawk();
        Card second = new BenevolentBodyguard();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects fewer than X target cards")
    void rejectsFewerThanXTargets() {
        Card first = new SuntailHawk();
        Card second = new BenevolentBodyguard();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RatsFeast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
    }

    @Test
    @DisplayName("Requires all X targets to come from one graveyard")
    void requiresOneGraveyard() {
        Card ownCard = new SuntailHawk();
        Card opponentCard = new BenevolentBodyguard();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RatsFeast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("With X=0, exiles no cards")
    void zeroExilesNothing() {
        Card graveyardCard = new SuntailHawk();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile exactly X cards from an opponent's graveyard")
    void exilesFromOpponentsGraveyard() {
        Card first = new BorderPatrol();
        Card second = new BorderPatrol();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Can cast with X=0 when both graveyards are empty")
    void zeroWithEmptyGraveyards() {
        RatsFeast spell = new RatsFeast();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target a noncreature card")
    void exilesNoncreatureCard() {
        RatsFeast target = new RatsFeast();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Rejects mixed-graveyard selections even when one graveyard has enough cards")
    void rejectsMixedGraveyardSelection() {
        Card ownFirst = new SuntailHawk();
        Card ownSecond = new BenevolentBodyguard();
        Card opponentCard = new BorderPatrol();
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RatsFeast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(ownFirst.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Still exiles a legal target when another target leaves the graveyard")
    void resolvesWithOneRemainingTarget() {
        Card departed = new SuntailHawk();
        Card remaining = new BenevolentBodyguard();
        RatsFeast spell = new RatsFeast();
        harness.setGraveyard(player2, List.of(departed, remaining));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(departed.getId(), remaining.getId()));

        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(departed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(departed);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
