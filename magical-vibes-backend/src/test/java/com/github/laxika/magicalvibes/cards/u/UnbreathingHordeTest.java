package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnbreathingHordeTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersPerSubtypeEffect as ETB effect")
    void hasCorrectETBEffect() {
        UnbreathingHorde card = new UnbreathingHorde();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersPerSubtypeEffect.class);
    }

    @Test
    @DisplayName("Has PreventDamageAndRemovePlusOnePlusOneCountersEffect with removeOneOnly=true")
    void hasCorrectStaticEffect() {
        UnbreathingHorde card = new UnbreathingHorde();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PreventDamageAndRemovePlusOnePlusOneCountersEffect.class);
        var effect = (PreventDamageAndRemovePlusOnePlusOneCountersEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.removeOneOnly()).isTrue();
    }

    // ===== ETB counter placement =====

    @Test
    @DisplayName("Enters with 0 counters when no other Zombies and empty graveyard, dies to SBA")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unbreathing Horde"));
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

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent horde = findHorde(player1);
        assertThat(horde).isNotNull();
        assertThat(horde.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with counters equal to Zombie cards in graveyard")
    void entersWithCountersFromGraveyardZombies() {
        // Put 3 Zombie cards in the graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DiregrafGhoul());
        gd.playerGraveyards.get(player1.getId()).add(new DiregrafGhoul());
        gd.playerGraveyards.get(player1.getId()).add(new DiregrafGhoul());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent horde = findHorde(player1);
        assertThat(horde).isNotNull();
        assertThat(horde.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with counters from both battlefield Zombies and graveyard Zombie cards")
    void entersWithCountersFromBothSources() {
        // 2 Zombies on battlefield + 1 in graveyard = 3 counters
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        gd.playerGraveyards.get(player1.getId()).add(new DiregrafGhoul());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent horde = findHorde(player1);
        assertThat(horde).isNotNull();
        assertThat(horde.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's Zombies or opponent's graveyard")
    void doesNotCountOpponentZombies() {
        // Opponent has Zombies on battlefield and in graveyard
        harness.addToBattlefield(player2, new DiregrafGhoul());
        gd.playerGraveyards.get(player2.getId()).add(new DiregrafGhoul());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // 0 counters from player1's perspective, dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unbreathing Horde"));
    }

    @Test
    @DisplayName("Does not count non-Zombie creatures")
    void doesNotCountNonZombies() {
        // GrizzlyBears is a Bear, not a Zombie
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UnbreathingHorde()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // 0 counters, dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unbreathing Horde"));
    }

    // ===== Damage prevention =====

    @Test
    @DisplayName("Damage is prevented and removes exactly one +1/+1 counter")
    void damagePreventedRemovesOneCounter() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findHorde(player2);
        horde.setPlusOnePlusOneCounters(3); // 3/3

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hordeId = horde.getId();
        harness.castInstant(player1, 0, hordeId);
        harness.passBothPriorities();

        // Shock deals 2 damage, but only 1 counter is removed (removeOneOnly=true)
        harness.assertOnBattlefield(player2, "Unbreathing Horde");
        Permanent survivingHorde = findHorde(player2);
        assertThat(survivingHorde.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple damage events each remove one counter")
    void multipleDamageEventsRemoveOneCounterEach() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findHorde(player2);
        horde.setPlusOnePlusOneCounters(3); // 3/3

        // First Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, horde.getId());
        harness.passBothPriorities();

        assertThat(findHorde(player2).getPlusOnePlusOneCounters()).isEqualTo(2);

        // Second Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, horde.getId());
        harness.passBothPriorities();

        assertThat(findHorde(player2).getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing last counter makes it 0/0, dies to SBA")
    void diesWhenLastCounterRemoved() {
        harness.addToBattlefield(player2, new UnbreathingHorde());
        Permanent horde = findHorde(player2);
        horde.setPlusOnePlusOneCounters(1); // 1/1

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, horde.getId());
        harness.passBothPriorities();

        // 0/0, dies to SBA
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unbreathing Horde"));
    }

    @Test
    @DisplayName("Combat damage is prevented and removes one counter")
    void combatDamageRemovesOneCounter() {
        UnbreathingHorde hordeCard = new UnbreathingHorde();
        Permanent blocker = new Permanent(hordeCard);
        blocker.setSummoningSick(false);
        blocker.setPlusOnePlusOneCounters(3); // 3/3
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Bears deal 2 combat damage, but only 1 counter is removed
        Permanent survivingHorde = findHorde(player2);
        assertThat(survivingHorde).isNotNull();
        assertThat(survivingHorde.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent findHorde(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Unbreathing Horde"))
                .findFirst().orElse(null);
    }
}
