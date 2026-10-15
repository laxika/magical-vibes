package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrippingDead;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbalmedBrawler.class, DrippingDead.class, FugitiveWizard.class})
class EmbalmedBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each Zombie card in its controller's hand")
    void entersWithCountersForZombiesInHand() {
        EmbalmedBrawler brawler = new EmbalmedBrawler();
        harness.setHand(player1, List.of(
                brawler, new DrippingDead(), new DrippingDead(), new FugitiveWizard()));
        harness.setHand(player2, List.of(new DrippingDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, gd.interaction
                .activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class).validCardIds());

        assertThat(findPermanent(player1, brawler.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Lets its controller choose how many Zombie cards to reveal for Amplify")
    void choosesHowManyZombieCardsToReveal() {
        EmbalmedBrawler brawler = new EmbalmedBrawler();
        DrippingDead firstZombie = new DrippingDead();
        DrippingDead secondZombie = new DrippingDead();
        harness.setHand(player1, List.of(brawler, firstZombie, secondZombie, new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, brawler.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its attack trigger makes its controller lose life equal to its counters")
    void losesLifeWhenAttacking() {
        Permanent brawler = addCreatureReady(player1, new EmbalmedBrawler());
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Its block trigger makes its controller lose life equal to its counters")
    void losesLifeWhenBlocking() {
        addCreatureReady(player1, new FugitiveWizard()).setAttacking(true);
        Permanent brawler = addCreatureReady(player2, new EmbalmedBrawler());
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }


    @Test
    @DisplayName("May decline to reveal Zombies and enters without counters")
    void mayRevealNoZombies() {
        EmbalmedBrawler brawler = new EmbalmedBrawler();
        DrippingDead zombie = new DrippingDead();
        harness.setHand(player1, List.of(brawler, zombie));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, brawler.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(zombie);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enters without counters when its controller has no other Zombies")
    void entersWithoutEligibleCards() {
        EmbalmedBrawler brawler = new EmbalmedBrawler();
        harness.setHand(player1, List.of(brawler, new FugitiveWizard()));
        harness.setHand(player2, List.of(new DrippingDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, brawler.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Attack life loss counts counters when the trigger resolves")
    void attackCountsCountersAtResolution() {
        Permanent brawler = addCreatureReady(player1, new EmbalmedBrawler());
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        brawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Attacking without +1/+1 counters causes no life loss")
    void attackWithoutCountersLosesNoLife() {
        addCreatureReady(player1, new EmbalmedBrawler());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

}
