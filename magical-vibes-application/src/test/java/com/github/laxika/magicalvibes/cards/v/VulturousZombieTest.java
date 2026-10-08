package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ConsultTheNecrosages;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.g.GlimpseTheUnthinkable;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulturousZombie.class, ElvesOfDeepShadow.class, GlimpseTheUnthinkable.class,
        LastGasp.class, LightningHelix.class, ConsultTheNecrosages.class})
class VulturousZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's spell is put into their graveyard")
    void getsCounterWhenOpponentSpellIsPutIntoGraveyard() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningHelix()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets one counter for each opponent card milled")
    void getsCounterForEachOpponentCardMilled() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setLibrary(player2, List.of(
                new ElvesOfDeepShadow(), new ElvesOfDeepShadow(), new ElvesOfDeepShadow(),
                new ElvesOfDeepShadow(), new ElvesOfDeepShadow(), new ElvesOfDeepShadow(),
                new ElvesOfDeepShadow(), new ElvesOfDeepShadow(), new ElvesOfDeepShadow(),
                new ElvesOfDeepShadow()));
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Gets a counter when an opponent's permanent is put into their graveyard")
    void getsCounterWhenOpponentPermanentIsPutIntoGraveyard() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvesOfDeepShadow());
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, elf.getId());
        resolveAllTriggers();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Elves of Deep Shadow");
    }

    @Test
    @DisplayName("Does not trigger for a card put into the controller's graveyard")
    void doesNotTriggerForOwnCard() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers for noncreature cards milled, only as many as actually enter the graveyard")
    void getsCountersForNoncreatureCardsInShortLibrary() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setLibrary(player2, List.of(new LastGasp(), new LightningHelix(), new GlimpseTheUnthinkable()));
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when its controller mills cards")
    void doesNotTriggerForOwnMilledCards() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setLibrary(player1, List.of(new ElvesOfDeepShadow(), new LastGasp()));
        harness.setHand(player1, List.of(new GlimpseTheUnthinkable()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Zombie gets its own counter for an opponent's card")
    void eachZombieTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setHand(player2, List.of(new LightningHelix()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a counter for each card discarded by an opponent")
    void getsCountersForOpponentDiscards() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setHand(player2, List.of(new ElvesOfDeepShadow(), new LastGasp()));
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
