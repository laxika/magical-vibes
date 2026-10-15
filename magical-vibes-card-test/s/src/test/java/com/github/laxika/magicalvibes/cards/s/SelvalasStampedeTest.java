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

@CardUsed({SelvalasStampede.class, Forest.class, GrizzlyBears.class,
        SolemnSimulacrum.class, ShinyImpetus.class})
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

    @Test
    void wildVotesPutAvailableCreaturesOntoBattlefieldWhenLibraryHasTooFew() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, bears));

        cast();
        vote(ChoiceContext.SelvalasStampedeChoice.WILD,
                ChoiceContext.SelvalasStampedeChoice.WILD);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void wildVotesReturnAllRevealedNoncreaturesWhenLibraryHasNoCreatures() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        cast();
        vote(ChoiceContext.SelvalasStampedeChoice.WILD,
                ChoiceContext.SelvalasStampedeChoice.WILD);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void freeVotesMayBeDeclinedAndDoNotRevealTheLibraryOrOfferSorceries() {
        GrizzlyBears bears = new GrizzlyBears();
        SelvalasStampede uncastSpell = new SelvalasStampede();
        Forest top = new Forest();
        harness.setHand(player1, List.of(new SelvalasStampede(), bears, uncastSpell));
        harness.setLibrary(player1, List.of(top));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.FREE,
                ChoiceContext.SelvalasStampedeChoice.FREE);

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class).validCardIds())
                .containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, uncastSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void emptyLibraryDoesNotPreventPuttingAPermanentFromHandForAFreeVote() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SelvalasStampede(), forest));
        harness.setLibrary(player1, List.of());
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.WILD,
                ChoiceContext.SelvalasStampedeChoice.FREE);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void votingStartsWithTheCasterAndWildUsesThatPlayersLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest opponentsTopCard = new Forest();
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(bears));
        harness.setLibrary(player1, List.of(opponentsTopCard));
        harness.castFromHand(player2, new SelvalasStampede(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.SelvalasStampedeChoice.WILD);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.SelvalasStampedeChoice.WILD);

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getCard)
                .containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsTopCard);
    }

    @Test
    void creaturePutOntoBattlefieldByFreeVoteTriggersItsEntersAbility() {
        SolemnSimulacrum solemn = new SolemnSimulacrum();
        harness.setHand(player1, List.of(new SelvalasStampede(), solemn));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.FREE,
                ChoiceContext.SelvalasStampedeChoice.FREE);
        harness.handleMultipleCardsChosen(player1, List.of(solemn.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
    }

    @Test
    void auraChosenForFreeVoteStaysInHandWhenThereIsNothingItCanEnchant() {
        ShinyImpetus aura = new ShinyImpetus();
        harness.setHand(player1, List.of(new SelvalasStampede(), aura));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        vote(ChoiceContext.SelvalasStampedeChoice.FREE,
                ChoiceContext.SelvalasStampedeChoice.FREE);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    private void cast() {
        harness.castFromHand(player1, new SelvalasStampede(), "{4}{G}{G}");
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
