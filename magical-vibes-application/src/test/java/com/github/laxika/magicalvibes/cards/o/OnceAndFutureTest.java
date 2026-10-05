package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnceAndFuture.class, Forest.class, GrizzlyBears.class})
class OnceAndFutureTest extends BaseCardTest {

    @Test
    void returnsOneCardToHandAndOneToLibraryWithoutAdamant() {
        Card returned = new GrizzlyBears();
        Card putOnTop = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, putOnTop), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        PendingInteraction.MultiGraveyardChoice secondChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(secondChoice.validCardIds()).containsExactly(putOnTop.getId());
        harness.handleMultipleCardsChosen(player1, List.of(putOnTop.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(putOnTop);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void returnsBothCardsToHandWithAdamant() {
        Card returned = new GrizzlyBears();
        Card alsoReturned = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, alsoReturned), true);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(alsoReturned.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returned.getId())
                        || card.getId().equals(alsoReturned.getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void secondTargetIsOptionalAndMustBeDifferent() {
        Card returned = new GrizzlyBears();
        prepareSpell(List.of(returned), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returned.getId()));
    }

    @Test
    void cannotBeCastWithoutAFirstTarget() {
        prepareSpell(List.of(), false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillResolvesAndExilesWhenOneTargetBecomesIllegal() {
        Card returned = new GrizzlyBears();
        Card putOnTop = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, putOnTop), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(putOnTop.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(returned);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(putOnTop);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void adamantRequiresAtLeastThreeGreenManaSpent(int greenMana) {
        Card returned = new Forest();
        Card secondTarget = new Forest();
        OnceAndFuture spell = new OnceAndFuture();
        harness.setGraveyard(player1, List.of(returned, secondTarget));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, 4 - greenMana);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        if (greenMana == 3) {
            assertThat(gd.playerHands.get(player1.getId())).contains(secondTarget);
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(secondTarget);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(secondTarget);
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondTarget);
        }
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void canDeclineSecondTargetEvenWhenAnotherCardIsAvailable(boolean adamant) {
        Card returned = new Forest();
        Card unchosen = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, unchosen), adamant);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned).doesNotContain(unchosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(unchosen);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotResolveOrExileItselfWhenAllTargetsBecomeIllegal(boolean adamant) {
        Card firstTarget = new Forest();
        Card secondTarget = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(firstTarget, secondTarget), adamant);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(secondTarget.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(firstTarget, secondTarget));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Once and Future");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstTarget, secondTarget);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(firstTarget, secondTarget);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stillReturnsFirstTargetWhenSecondTargetBecomesIllegal(boolean adamant) {
        Card firstTarget = new Forest();
        Card secondTarget = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(firstTarget, secondTarget), adamant);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(secondTarget.getId()));
        harness.setGraveyard(player1, List.of(firstTarget));
        harness.setExile(player1, List.of(secondTarget));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstTarget).doesNotContain(secondTarget);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(secondTarget);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void cannotChooseOpponentsGraveyardCard() {
        Card ownCard = new Forest();
        Card opponentsCard = new Forest();
        prepareSpell(List.of(ownCard), false);
        harness.setGraveyard(player2, List.of(opponentsCard));

        castAndBeginTargeting();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentsCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotDeclineRequiredFirstTarget() {
        prepareSpell(List.of(new Forest()), false);

        castAndBeginTargeting();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCardForBothTargets() {
        Card firstTarget = new Forest();
        Card otherCard = new Forest();
        prepareSpell(List.of(firstTarget, otherCard), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId()));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotExileItselfWhenItsOnlyTargetBecomesIllegal() {
        Card target = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(target), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Once and Future");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
    }

    private OnceAndFuture prepareSpell(List<Card> graveyard, boolean adamant) {
        harness.setGraveyard(player1, graveyard);
        OnceAndFuture spell = new OnceAndFuture();
        harness.setHand(player1, List.of(spell));
        if (adamant) {
            harness.addMana(player1, ManaColor.GREEN, 4);
        } else {
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
        }
        return spell;
    }

    private void castAndBeginTargeting() {
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }
}
