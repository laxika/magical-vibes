package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberSwallower.class, Forest.class, Mountain.class, NessianCourser.class, VoyagesEnd.class})
class EmberSwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices three lands, or all their lands if they control fewer")
    void becomingMonstrousSacrificesThreeLandsFromEachPlayer() {
        Permanent swallower = addReadySwallower();
        addLands(player1, Mountain::new, 3);
        addLands(player2, Forest::new, 2);
        harness.addToBattlefield(player2, new NessianCourser());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(swallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(swallower.isMonstrous()).isTrue();
        assertThat(countLands(player1)).isZero();
        assertThat(countLands(player2)).isZero();
        harness.assertOnBattlefield(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("Each player chooses three lands when they control more than three")
    void eachPlayerChoosesThreeLandsWhenTheyControlMoreThanThree() {
        addReadySwallower();
        addLands(player1, Mountain::new, 4);
        addLands(player2, Forest::new, 4);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1,
                findPermanents(player1, "Mountain").stream().limit(3).map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player2,
                findPermanents(player2, "Forest").stream().limit(3).map(Permanent::getId).toList());

        assertThat(countLands(player1)).isEqualTo(1);
        assertThat(countLands(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing when already monstrous")
    void activatingMonstrosityAgainDoesNothing() {
        Permanent swallower = addReadySwallower();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        addLands(player1, Mountain::new, 1);
        addLands(player2, Forest::new, 1);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(swallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(swallower.isMonstrous()).isTrue();
        assertThat(countLands(player1)).isEqualTo(1);
        assertThat(countLands(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Queued monstrosity activations produce counters and a land sacrifice only once")
    void queuedActivationsBecomeMonstrousOnlyOnce() {
        Permanent swallower = addReadySwallower();
        addLands(player1, Mountain::new, 4);
        addLands(player2, Forest::new, 4);
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1,
                findPermanents(player1, "Mountain").stream().limit(3).map(Permanent::getId).toList());
        assertThat(countLands(player1)).isEqualTo(4);
        assertThat(countLands(player2)).isEqualTo(4);
        harness.handleMultiplePermanentsChosen(player2,
                findPermanents(player2, "Forest").stream().limit(3).map(Permanent::getId).toList());
        resolveAllTriggers();

        assertThat(swallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(swallower.isMonstrous()).isTrue();
        assertThat(countLands(player1)).isEqualTo(1);
        assertThat(countLands(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Ember Swallower before monstrosity resolves prevents the land sacrifice")
    void removingSourceBeforeResolutionDoesNotTriggerSacrifice() {
        Permanent swallower = addReadySwallower();
        addLands(player1, Mountain::new, 3);
        addLands(player2, Forest::new, 3);
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, swallower.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Ember Swallower");
        harness.assertNotOnBattlefield(player1, "Ember Swallower");
        assertThat(countLands(player1)).isEqualTo(3);
        assertThat(countLands(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("An already-triggered land sacrifice resolves after Ember Swallower leaves")
    void removingSourceAfterBecomingMonstrousDoesNotStopSacrifice() {
        Permanent swallower = addReadySwallower();
        addLands(player1, Mountain::new, 3);
        addLands(player2, Forest::new, 3);
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(swallower.isMonstrous()).isTrue();
        assertThat(countLands(player1)).isEqualTo(3);
        assertThat(countLands(player2)).isEqualTo(3);

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, swallower.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Ember Swallower");
        assertThat(countLands(player1)).isZero();
        assertThat(countLands(player2)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Ember Swallower can become monstrous on its opponent's turn")
    void monstrosityCanBeActivatedWhileSummoningSickOnOpponentsTurn() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new EmberSwallower());
        swallower.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(swallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(swallower.isMonstrous()).isTrue();
    }

    private Permanent addReadySwallower() {
        return addCreatureReady(player1, new EmberSwallower());
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, Supplier<Card> landSupplier, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, landSupplier.get());
        }
    }

    private long countLands(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
