package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SzadekLordOfSecrets.class, SkyknightLegionnaire.class, JaceBeleren.class, Humility.class})
class SzadekLordOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces its combat damage with +1/+1 counters and milling")
    void replacesCombatDamageWithCountersAndMill() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(
                new SkyknightLegionnaire(), new SkyknightLegionnaire(), new SkyknightLegionnaire(),
                new SkyknightLegionnaire(), new SkyknightLegionnaire()
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
        Permanent attacker = addCreatureReady(player1, new SkyknightLegionnaire());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SkyknightLegionnaire(), new SkyknightLegionnaire()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not replace combat damage dealt to a creature")
    void doesNotReplaceCombatDamageToCreature() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SkyknightLegionnaire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SkyknightLegionnaire(), new SkyknightLegionnaire()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not replace combat damage dealt to a planeswalker")
    void doesNotReplaceCombatDamageToPlaneswalker() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 10);
        szadek.setAttacking(true);
        szadek.setAttackTarget(jace.getId());
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SkyknightLegionnaire(), new SkyknightLegionnaire()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counters use the damage amount even when the library has fewer cards")
    void countersAreNotLimitedByLibrarySize() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SkyknightLegionnaire()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Existing counters increase the amount replaced on the next attack")
    void existingCountersIncreaseCountersAndMill() {
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        szadek.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, java.util.stream.IntStream.range(0, 12)
                .mapToObj(i -> new SkyknightLegionnaire()).toList());

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(15);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Humility removes Szadek's combat damage replacement")
    void losingAbilitiesRestoresNormalCombatDamage() {
        harness.addToBattlefield(player2, new Humility());
        Permanent szadek = addCreatureReady(player1, new SzadekLordOfSecrets());
        szadek.setAttacking(true);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SkyknightLegionnaire(), new SkyknightLegionnaire()));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(szadek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
