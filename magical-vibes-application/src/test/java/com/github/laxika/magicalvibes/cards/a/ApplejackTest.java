package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Applejack.class, Forest.class, GrizzlyBears.class})
class ApplejackTest extends BaseCardTest {

    @Test
    void wingsPutFlyingToyTokenOntoBattlefield() {
        Card toy = toy("Winged Toy", CardColor.BLUE,
                List.of(CardSubtype.TOY, CardSubtype.PEGASUS), Set.of(Keyword.FLYING));
        beginGathering(toy);

        answerToy(toy);

        Permanent token = findPermanent(player1, "Winged Toy");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(toy);
    }

    @Test
    void hornScriesAfterPuttingToyTokenOntoBattlefield() {
        Card toy = toy("Horned Toy", CardColor.WHITE,
                List.of(CardSubtype.TOY, CardSubtype.UNICORN), Set.of());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        beginGathering(toy);

        answerToy(toy);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(findPermanent(player1, "Horned Toy")).isNotNull();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void toyWithoutWingsOrHornCreatesFood() {
        Card toy = toy("Plain Toy", CardColor.GREEN,
                List.of(CardSubtype.TOY), Set.of());
        beginGathering(toy);

        answerToy(toy);

        assertThat(findPermanent(player1, "Plain Toy")).isNotNull();
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    private void beginGathering(Card toy) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(toy)));
        harness.addToBattlefield(player1, new Applejack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ApplejackToyChoice.class))
                .isNotNull();
    }

    private void answerToy(Card toy) {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(toy.getId())));
    }

    private Card toy(String name, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setColors(List.of(color));
        card.setSubtypes(subtypes);
        card.setKeywords(Set.copyOf(keywords));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
