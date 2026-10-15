package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CommenceTheEndgame;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KasminasTransmutation;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarsetParterOfVeils.class, Divination.class, GrizzlyBears.class, Plains.class, Shock.class,
        CommenceTheEndgame.class, KasminasTransmutation.class, SarkhanTheMasterless.class})
class NarsetParterOfVeilsTest extends BaseCardTest {

    @Test
    @DisplayName("Limits each opponent to one actual card draw each turn")
    void limitsOpponentDraws() {
        addNarset(player1);
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setHand(player2, List.of(new Divination()));
        addDivinationMana(player2);
        prepareMainPhase(player2);

        harness.castAndResolveSorcery(player2, 0, (UUID) null);

        assertThat(gd.playerHands.get(player2.getId())).contains(firstCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Does not limit the controller's own draws")
    void doesNotLimitControllerDraws() {
        addNarset(player1);
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new Divination()));
        addDivinationMana(player1);
        prepareMainPhase(player1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Minus two offers only noncreature nonland cards from the top four")
    void minusTwoSelectsEligibleCard() {
        Permanent narset = addReadyNarset(player1);
        Card eligible = new Shock();
        Card creature = new GrizzlyBears();
        Card land = new Plains();
        Card secondLand = new Plains();
        harness.setLibrary(player1, List.of(creature, eligible, land, secondLand));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature, land, secondLand);
        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Minus two may decline an eligible card and leaves unexamined cards on top")
    void minusTwoMayDeclineEligibleCard() {
        addReadyNarset(player1);
        Card eligible = new NarsetParterOfVeils();
        Card firstLand = new Plains();
        Card secondLand = new Plains();
        Card thirdLand = new Plains();
        Card untouched = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(eligible, firstLand, secondLand, thirdLand, untouched));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(eligible, firstLand, secondLand, thirdLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Minus two puts all looked-at cards on bottom when none is eligible")
    void minusTwoWithNoEligibleCards() {
        addReadyNarset(player1);
        Card firstLand = new Plains();
        Card secondLand = new Plains();
        Card thirdLand = new Plains();
        Card fourthLand = new Plains();
        Card untouched = new NarsetParterOfVeils();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstLand, secondLand, thirdLand, fourthLand, untouched));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(firstLand, secondLand, thirdLand, fourthLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Minus two can choose from a library with fewer than four cards")
    void minusTwoWithShortLibrary() {
        addReadyNarset(player1);
        Card eligible = new NarsetParterOfVeils();
        Card land = new Plains();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(eligible, land));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Minus two resolves with an empty library without attempting a draw")
    void minusTwoWithEmptyLibrary() {
        Permanent narset = addReadyNarset(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Counts draws made before Narset entered the battlefield")
    void countsEarlierDraws() {
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        Card fourth = new Plains();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        harness.setHand(player2, List.of(new Divination(), new Divination()));
        prepareMainPhase(player2);
        addDivinationMana(player2);
        harness.castAndResolveSorcery(player2, 0, (UUID) null);
        addNarset(player1);
        addDivinationMana(player2);

        harness.castAndResolveSorcery(player2, 0, (UUID) null);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, fourth);
    }

    @Test
    @DisplayName("Limits opponent draws during the controller's turn too")
    void limitsDrawsOnControllersTurn() {
        addReadyNarset(player1);
        Card first = new Plains();
        Card second = new Plains();
        harness.setLibrary(player2, List.of(first, second));

        castOpponentDrawSpell();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Stops restricting draws when animated Narset loses all abilities")
    void doesNotRestrictDrawsAfterLosingAbilities() {
        Permanent narset = addReadyNarset(player1);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new KasminasTransmutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, narset.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostPrintedAbilities(gd, narset)).isTrue();
        Card first = new Plains();
        Card second = new Plains();
        harness.setLibrary(player2, List.of(first, second));

        castOpponentDrawSpell();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void castOpponentDrawSpell() {
        harness.setHand(player2, List.of(new CommenceTheEndgame()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
    }

    private Permanent addReadyNarset(Player player) {
        Permanent narset = addNarset(player);
        narset.setSummoningSick(false);
        prepareMainPhase(player);
        return narset;
    }

    private Permanent addNarset(Player player) {
        Permanent narset = harness.addToBattlefieldAndReturn(player, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 5);
        return narset;
    }

    private void addDivinationMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
