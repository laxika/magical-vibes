package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({NarsetParterOfVeils.class, Divination.class, GrizzlyBears.class, Plains.class, Shock.class})
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

        harness.castSorcery(player2, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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
