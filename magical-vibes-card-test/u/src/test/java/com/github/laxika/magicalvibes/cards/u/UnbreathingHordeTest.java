package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.d.DemonmailHauberk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({UnbreathingHorde.class, DiregrafGhoul.class, DarkthicketWolf.class,
        BrimstoneVolley.class, UnburialRites.class, DemonmailHauberk.class})
class UnbreathingHordeTest extends BaseCardTest {


    @Test
    @DisplayName("Enters with 0 counters when no other Zombies and empty graveyard, dies to SBA")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        harness.assertNotOnBattlefield(player1, "Unbreathing Horde");
    }

    @Test
    @DisplayName("Enters with counters equal to other Zombies on battlefield")
    void entersWithCountersFromBattlefieldZombies() {
        // Put 2 Zombies on the battlefield
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent horde = findPermanent(player1, "Unbreathing Horde");
        assertThat(horde).isNotNull();
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with counters equal to Zombie cards in graveyard")
    void entersWithCountersFromGraveyardZombies() {
        // Put 3 Zombie cards in the graveyard
        harness.setGraveyard(player1, List.of(new DiregrafGhoul(), new DiregrafGhoul(), new DiregrafGhoul()));

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent horde = findPermanent(player1, "Unbreathing Horde");
        assertThat(horde).isNotNull();
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with counters from both battlefield Zombies and graveyard Zombie cards")
    void entersWithCountersFromBothSources() {
        // 2 Zombies on battlefield + 1 in graveyard = 3 counters
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setGraveyard(player1, List.of(new DiregrafGhoul()));

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent horde = findPermanent(player1, "Unbreathing Horde");
        assertThat(horde).isNotNull();
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's Zombies or opponent's graveyard")
    void doesNotCountOpponentZombies() {
        // Opponent has Zombies on battlefield and in graveyard
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.setGraveyard(player2, List.of(new DiregrafGhoul()));

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0 counters from player1's perspective, dies to SBA
        harness.assertNotOnBattlefield(player1, "Unbreathing Horde");
    }

    @Test
    @DisplayName("Does not count non-Zombie creatures")
    void doesNotCountNonZombies() {
        // DarkthicketWolf is a Wolf, not a Zombie
        harness.addToBattlefield(player1, new DarkthicketWolf());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0 counters, dies to SBA
        harness.assertNotOnBattlefield(player1, "Unbreathing Horde");
    }


    @Test
    @DisplayName("Damage is prevented and removes exactly one +1/+1 counter")
    void damagePreventedRemovesOneCounter() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findPermanent(player2, "Unbreathing Horde");
        horde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 3/3

        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID hordeId = horde.getId();
        harness.castAndResolveInstant(player1, 0, hordeId);

        // BrimstoneVolley deals 3 damage, but only 1 counter is removed (removeOneOnly=true)
        harness.assertOnBattlefield(player2, "Unbreathing Horde");
        Permanent survivingHorde = findPermanent(player2, "Unbreathing Horde");
        assertThat(survivingHorde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple damage events each remove one counter")
    void multipleDamageEventsRemoveOneCounterEach() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findPermanent(player2, "Unbreathing Horde");
        horde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 3/3

        // First BrimstoneVolley
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, horde.getId());

        assertThat(findPermanent(player2, "Unbreathing Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        // Second BrimstoneVolley
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, horde.getId());

        assertThat(findPermanent(player2, "Unbreathing Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing last counter makes it 0/0, dies to SBA")
    void diesWhenLastCounterRemoved() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findPermanent(player2, "Unbreathing Horde");
        horde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 1/1

        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, horde.getId());

        // 0/0, dies to SBA
        harness.assertNotOnBattlefield(player2, "Unbreathing Horde");
    }

    @Test
    @DisplayName("Combat damage is prevented and removes one counter")
    void combatDamageRemovesOneCounter() {
        Permanent blocker = addCreatureReady(player2, new UnbreathingHorde());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // 3/3
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player1, new DarkthicketWolf());
        attacker.setAttacking(true);

        resolveCombat();

        // Wolves deal 2 combat damage, but only 1 counter is removed
        Permanent survivingHorde = findPermanent(player2, "Unbreathing Horde");
        assertThat(survivingHorde).isNotNull();
        assertThat(survivingHorde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Counts itself when entering from its controller's graveyard")
    void countsItselfWhenReanimated() {
        UnbreathingHorde card = new UnbreathingHorde();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, card.getId());

        harness.assertOnBattlefield(player1, "Unbreathing Horde");
        assertThat(findPermanent(player1, "Unbreathing Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reanimation counts itself together with other battlefield and graveyard Zombies")
    void reanimationCountsAllThreeContributions() {
        UnbreathingHorde card = new UnbreathingHorde();
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setGraveyard(player1, List.of(card, new DiregrafGhoul(), new DarkthicketWolf()));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, card.getId());

        harness.assertOnBattlefield(player1, "Unbreathing Horde");
        assertThat(findPermanent(player1, "Unbreathing Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents damage even with no counters when equipment keeps it alive")
    void preventsDamageWithoutCounters() {
        Permanent horde = harness.addToBattlefieldAndReturn(player2, new UnbreathingHorde());
        Permanent hauberk = harness.addToBattlefieldAndReturn(player2, new DemonmailHauberk());
        hauberk.setAttachedTo(horde.getId());
        harness.setHand(player1, List.of(new BrimstoneVolley(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, horde.getId());
        harness.castAndResolveInstant(player1, 0, horde.getId());

        harness.assertOnBattlefield(player2, "Unbreathing Horde");
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(horde.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Simultaneous combat damage from two blockers removes only one counter")
    void simultaneousCombatDamageRemovesOneCounter() {
        Permanent horde = addCreatureReady(player1, new UnbreathingHorde());
        horde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Permanent first = addCreatureReady(player2, new DarkthicketWolf());
        Permanent second = addCreatureReady(player2, new DarkthicketWolf());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 3, second.getId(), 2));

        harness.assertOnBattlefield(player1, "Unbreathing Horde");
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(horde.getMarkedDamage()).isZero();
    }

}
