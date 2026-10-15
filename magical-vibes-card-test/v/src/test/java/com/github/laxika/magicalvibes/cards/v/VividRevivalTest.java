package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosChallenger;
import com.github.laxika.magicalvibes.cards.c.CentaurPeacemaker;
import com.github.laxika.magicalvibes.cards.t.ThoughtErasure;
import com.github.laxika.magicalvibes.cards.g.GenerousStray;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VividRevival.class, ThoughtErasure.class, BorosChallenger.class,
        CentaurPeacemaker.class, GenerousStray.class})
class VividRevivalTest extends BaseCardTest {

    @Test
    void returnsUpToThreeMulticoloredCardsAndExilesItself() {
        Card first = new ThoughtErasure();
        Card second = new BorosChallenger();
        Card third = new CentaurPeacemaker();
        Card monocolored = new GenerousStray();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(first, second, third, monocolored));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(third.getId(), monocolored.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    void exilesItselfWhenNoMulticoloredCardsAreInTheGraveyard() {
        Card monocolored = new GenerousStray();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(monocolored));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(monocolored.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    void canChooseZeroTargetsEvenWhenMulticoloredCardsAreAvailable() {
        Card target = new ThoughtErasure();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    void returnsAllThreeTargetsAndDoesNotOfferOpponentsCards() {
        Card first = new ThoughtErasure();
        Card second = new BorosChallenger();
        Card third = new CentaurPeacemaker();
        Card opposing = new ThoughtErasure();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetLeavesTheGraveyard() {
        Card removed = new ThoughtErasure();
        Card remaining = new BorosChallenger();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(removed.getId(), spell.getId());
    }

    @Test
    void goesToGraveyardInsteadOfExileWhenAllChosenTargetsBecomeIllegal() {
        Card target = new ThoughtErasure();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(target.getId()).doesNotContain(spell.getId());
    }
}
