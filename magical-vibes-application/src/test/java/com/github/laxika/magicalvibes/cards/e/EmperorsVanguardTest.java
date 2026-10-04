package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmperorsVanguard.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class EmperorsVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Deals combat damage and explores with land on top — land goes to hand")
    void exploreLandGoesToHand() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        resolveCombatAndExploreTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Deals combat damage and explores with land on top — no +1/+1 counter")
    void exploreLandNoCounter() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        resolveCombatAndExploreTrigger();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Deals combat damage and explores with non-land on top — gets +1/+1 counter")
    void exploreNonLandAddsCounter() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        resolveCombatAndExploreTrigger();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals combat damage and explores with non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        resolveCombatAndExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Deals combat damage and explores with non-land — decline leaves card on top")
    void exploreNonLandDeclineLeavesOnTop() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        resolveCombatAndExploreTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("No explore trigger when blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);

        // 4/4 blocker — Vanguard is 4/3, both die
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        resolveCombat();

        // Vanguard should be dead — no explore trigger
        harness.assertInGraveyard(player1, "Emperor's Vanguard");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Deals 4 combat damage to defending player")
    void dealsCombatDamage() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Exploring with an empty library still adds a counter")
    void emptyLibraryStillAddsCounter() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        gd.playerDecks.get(player1.getId()).clear();

        resolveCombatAndExploreTrigger();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Explores even if Vanguard leaves before its trigger resolves")
    void exploresAfterLeavingBattlefield() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(vanguard);
        gd.playerGraveyards.get(player1.getId()).add(vanguard.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Explores its current controller's library after changing control")
    void exploresCurrentControllersLibrary() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        Card originalControllersLand = new Forest();
        Card currentControllersLand = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(originalControllersLand);
        gd.playerDecks.get(player2.getId()).addFirst(currentControllersLand);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(vanguard);
        vanguard.setAttacking(false);
        gd.playerBattlefields.get(player2.getId()).add(vanguard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(currentControllersLand);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(originalControllersLand);
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addVanguardReady(Player player) {
        return addCreatureReady(player, new EmperorsVanguard());
    }

    private void resolveCombatAndExploreTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve explore trigger
    }
}
