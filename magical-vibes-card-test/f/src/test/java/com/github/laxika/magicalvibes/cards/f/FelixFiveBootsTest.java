package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HoardRobber;
import com.github.laxika.magicalvibes.cards.o.OhranFrostfang;
import com.github.laxika.magicalvibes.cards.o.OrochiSoulReaver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelixFiveBoots.class, HoardRobber.class, OhranFrostfang.class, OrochiSoulReaver.class})
class FelixFiveBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Felix doubles triggers caused by a creature dealing combat damage to a player")
    void doublesCombatDamageTriggers() {
        addCreatureReady(player1, new FelixFiveBoots());
        Permanent robber = addCreatureReady(player1, new HoardRobber());
        robber.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Felix does not double a combat-damage trigger from an opponent's creature")
    void doesNotDoubleOpponentCombatDamageTrigger() {
        addCreatureReady(player1, new FelixFiveBoots());
        Permanent robber = addCreatureReady(player2, new HoardRobber());
        robber.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Felix's own combat damage doubles a different permanent's trigger")
    void doublesWatcherTriggerFromFelixDamage() {
        Permanent felix = addCreatureReady(player1, new FelixFiveBoots());
        addCreatureReady(player1, new OhranFrostfang());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FelixFiveBoots(), new FelixFiveBoots(), new FelixFiveBoots()));
        felix.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each connecting creature produces two watcher triggers")
    void doublesEachCreatureDamageTrigger() {
        Permanent felix = addCreatureReady(player1, new FelixFiveBoots());
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FelixFiveBoots(), new FelixFiveBoots(),
                new FelixFiveBoots(), new FelixFiveBoots(), new FelixFiveBoots()));
        felix.setAttacking(true);
        frostfang.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Losing all abilities disables Felix's additional trigger effect")
    void doesNotDoubleAfterLosingAbilities() {
        Permanent felix = addCreatureReady(player1, new FelixFiveBoots());
        addCreatureReady(player1, new OhranFrostfang());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FelixFiveBoots(), new FelixFiveBoots()));
        felix.setLosesAllAbilitiesUntilEndOfTurn(true);
        felix.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A one-or-more combat damage trigger fires twice for multiple connecting creatures")
    void doublesBatchedDamageTriggerOnlyOnce() {
        Permanent felix = addCreatureReady(player1, new FelixFiveBoots());
        Permanent reaver = addCreatureReady(player1, new OrochiSoulReaver());
        harness.setLibrary(player2, List.of(new FelixFiveBoots(), new FelixFiveBoots(), new FelixFiveBoots()));
        felix.setAttacking(true);
        reaver.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isFaceDown)).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
