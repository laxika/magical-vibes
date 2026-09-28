package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelvalasStampede.class, Forest.class, GrizzlyBears.class})
class SelvalasStampedeTest extends BaseCardTest {

    @Test
    void wildVotesPutThatManyCreatureCardsFromLibraryOntoBattlefield() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), first, new Forest(), second));

        cast();
        vote(ChoiceContext.SelvalasStampedeChoice.WILD,
                ChoiceContext.SelvalasStampedeChoice.WILD);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }

    @Test
    void freeVotesPutUpToThatManyPermanentCardsFromHandOntoBattlefield() {
        SelvalasStampede spell = new SelvalasStampede();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell, bears, forest));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.FREE,
                ChoiceContext.SelvalasStampedeChoice.FREE);

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(bears, forest);
    }

    @Test
    void splitVoteResolvesBothBranches() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest forest = new Forest();
        SelvalasStampede spell = new SelvalasStampede();
        harness.setHand(player1, List.of(spell, forest));
        harness.setLibrary(player1, List.of(new Forest(), creature));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.WILD,
                ChoiceContext.SelvalasStampedeChoice.FREE);

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(creature, forest);
    }

    private void cast() {
        SelvalasStampede spell = new SelvalasStampede();
        harness.setHand(player1, List.of(spell));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 6);
    }

    private void vote(String firstVote, String secondVote) {
        harness.handleListChoice(player1, firstVote);
        harness.handleListChoice(player2, secondVote);
    }
}
