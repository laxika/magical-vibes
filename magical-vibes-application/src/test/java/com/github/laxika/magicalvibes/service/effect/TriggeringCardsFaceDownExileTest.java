package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.ExiledCardCounter;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeOnlyCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyChangeCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlotExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "That card" of an event that exiled it face down (CR 400.7e): it is named, a filter sees no characteristics
 * (CR 406.3a), and nothing the opponent receives names it while it stays hidden.
 */
@CardUsed({Forest.class, GrizzlyBears.class, Pacifism.class})
class TriggeringCardsFaceDownExileTest extends BaseCardTest {

    private void resolveAbout(Card exiledFaceDown, CardEffect effect) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.inMutationScope(() -> gd.addToExile(player1.getId(), exiledFaceDown, null, true));
        Card source = new Card();
        source.setName("Fixture Source");
        source.setType(CardType.ARTIFACT);
        StackEntry ability = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player1.getId(),
                "Fixture Source's ability", List.of(effect));
        ability.setTriggeringCardId(exiledFaceDown.getId());
        ability.setTriggeringCardExileEntryVersion(
                gd.exileEntryVersions.getOrDefault(exiledFaceDown.getId(), 0L));
        harness.inMutationScope(() -> gd.stack.add(ability));
        harness.getConn2().clearMessages();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void assertOpponentNeverSeesName(Card card) {
        assertThat(harness.getConn2().getSentMessages()).isNotEmpty()
                .noneMatch(message -> message.contains(card.getName()));
        assertThat(gameLogContains(card.getName())).isFalse();
    }

    @Test
    @DisplayName("The card is put into its owner's hand and the opponent doesn't see its name")
    void intoHandUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PutCardsIntoHandEffect(Subjects.triggeringCards()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(card.getId());
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("A type filter doesn't match the hidden face")
    void typeFilterDoesNotSeeTheHiddenFace() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PutCardsIntoHandEffect(
                Subjects.triggeringCards(new CardTypePredicate(CardType.CREATURE))));

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("A nonland filter matches a hidden land, which has no characteristics")
    void negativeFilterDoesNotSeeTheHiddenFace() {
        Card card = new Forest();

        resolveAbout(card, new PutCardsIntoHandEffect(
                Subjects.triggeringCards(new CardNotPredicate(new CardTypePredicate(CardType.LAND)))));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(card.getId());
    }

    @Test
    @DisplayName("Choosing the card doesn't tell the opponent its name")
    void choiceUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, ChooseCardEffect.oneOf(Subjects.triggeringCards(), "a card"));

        assertThat(gameLogContains("chooses a face-down card")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("A perpetual cost change doesn't name the card")
    void perpetualCostUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PerpetuallyChangeCastCostEffect(Subjects.triggeringCards(), -1));

        assertThat(gameLogContains("perpetually costs 1 generic mana less")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("A perpetual type change doesn't name the card")
    void perpetualTypeUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PerpetuallyBecomeOnlyCardTypeEffect(Subjects.triggeringCards(), CardType.ARTIFACT));

        assertThat(gameLogContains("perpetually becomes only artifact")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("A counter put on the card doesn't name it")
    void counterUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PutCounterOnExiledCardsEffect(Subjects.triggeringCards(), ExiledCardCounter.MEMORY));

        assertThat(gameLogContains("gets a memory counter")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("Time counters put on the card don't name it")
    void suspendUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PutTimeCountersAndGainSuspendEffect(Subjects.triggeringCards(), 3));

        assertThat(gameLogContains("gets 3 time counters")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("Plotting the card while it is face down doesn't name it")
    void plotUnnamed() {
        Card card = new GrizzlyBears();

        resolveAbout(card, new PlotExiledCardsEffect(Subjects.triggeringCards()));

        assertThat(gameLogContains("becomes plotted")).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("An Aura with nothing to enchant stays in exile unnamed")
    void auraThatStaysIsUnnamed() {
        Card card = new Pacifism();

        resolveAbout(card, new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards()));

        assertThat(gd.findExiledCard(card.getId()).faceDown()).isTrue();
        assertOpponentNeverSeesName(card);
    }

    @Test
    @DisplayName("An Aura's host choice isn't shown to the opponent, and it then enters face up")
    void auraHostChoiceIsUnnamedToTheOpponent() {
        var first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card card = new Pacifism();

        resolveAbout(card, new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertOpponentNeverSeesName(card);

        harness.handlePermanentChosen(player1, first.getId());

        assertThat(findPermanent(player1, "Pacifism").isFaceDown()).isFalse();
    }
}
