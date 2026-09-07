package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SzadekLordOfSecrets.class, GrizzlyBears.class})
class SzadekLordOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces its combat damage with +1/+1 counters and milling")
    void replacesCombatDamageWithCountersAndMill() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()
        ));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Does not replace another creature's combat damage")
    void doesNotReplaceAnotherCreatureCombatDamage() {
        harness.addToBattlefield(player1, new SzadekLordOfSecrets());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not replace combat damage dealt to a creature")
    void doesNotReplaceCombatDamageToCreature() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
