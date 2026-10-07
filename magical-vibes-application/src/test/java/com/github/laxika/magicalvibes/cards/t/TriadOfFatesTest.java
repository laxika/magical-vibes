package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriadOfFates.class, TravelingPhilosopher.class})
class TriadOfFatesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a fate counter on another target creature")
    void putsFateCounterOnAnotherCreature() {
        Permanent triad = addReadyTriad(player1);
        Permanent philosopher = addReadyPhilosopher(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, philosopher.getId());
        harness.passBothPriorities();

        assertThat(triad.getCounterCount(CounterType.FATE)).isZero();
        assertThat(philosopher.getCounterCount(CounterType.FATE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot put a fate counter on Triad of Fates itself")
    void cannotTargetItselfForFateCounter() {
        addReadyTriad(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Triad of Fates")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles a fate-countered creature and returns it without its counter")
    void flickersFateCounteredCreature() {
        addReadyTriad(player1);
        Permanent philosopher = addReadyPhilosopher(player1);
        philosopher.setCounterCount(CounterType.FATE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, philosopher.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Traveling Philosopher");
        assertThat(returned.getId()).isNotEqualTo(philosopher.getId());
        assertThat(returned.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    @DisplayName("Exiles a fate-countered creature and its controller draws two cards")
    void exilesFateCounteredCreatureAndItsControllerDraws() {
        addReadyTriad(player1);
        Permanent philosopher = addReadyPhilosopher(player2);
        philosopher.setCounterCount(CounterType.FATE, 1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, philosopher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Traveling Philosopher"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a creature without a fate counter for the white ability")
    void cannotFlickerCreatureWithoutFateCounter() {
        addReadyTriad(player1);
        Permanent philosopher = addReadyPhilosopher(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, philosopher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutFateCounterOnOpponentsCreature() {
        Permanent triad = addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(1);
        assertThat(triad.isTapped()).isTrue();
    }

    @Test
    void cannotExileCreatureWithoutFateCounter() {
        addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whiteAbilityDoesNothingWhenLastFateCounterIsRemoved() {
        addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player2);
        target.setCounterCount(CounterType.FATE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setCounterCount(CounterType.FATE, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Traveling Philosopher").getId()).isEqualTo(target.getId());
    }

    @Test
    void blackAbilityDoesNotExileOrDrawWhenLastFateCounterIsRemoved() {
        addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player2);
        target.setCounterCount(CounterType.FATE, 1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        target.setCounterCount(CounterType.FATE, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Traveling Philosopher").getId()).isEqualTo(target.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void canFlickerTriadItselfWithFateCounter() {
        Permanent triad = addReadyTriad(player1);
        triad.setCounterCount(CounterType.FATE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, triad.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Triad of Fates");
        assertThat(returned.getId()).isNotEqualTo(triad.getId());
        assertThat(returned.getCounterCount(CounterType.FATE)).isZero();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void canExileTriadItselfAndDrawTwoCards() {
        Permanent triad = addReadyTriad(player1);
        triad.setCounterCount(CounterType.FATE, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, triad.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Triad of Fates");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(triad.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void flickerReturnsStolenCreatureToOwner() {
        addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player1);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        target.setCounterCount(CounterType.FATE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Traveling Philosopher");
        Permanent returned = findPermanent(player2, "Traveling Philosopher");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    void stolenCreaturesControllerRatherThanOwnerDraws() {
        addReadyTriad(player1);
        Permanent target = addReadyPhilosopher(player2);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        target.setCounterCount(CounterType.FATE, 1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyTriad(Player player) {
        Permanent triad = harness.addToBattlefieldAndReturn(player, new TriadOfFates());
        triad.setSummoningSick(false);
        return triad;
    }

    private Permanent addReadyPhilosopher(Player player) {
        Permanent philosopher = harness.addToBattlefieldAndReturn(player, new TravelingPhilosopher());
        philosopher.setSummoningSick(false);
        return philosopher;
    }
}
