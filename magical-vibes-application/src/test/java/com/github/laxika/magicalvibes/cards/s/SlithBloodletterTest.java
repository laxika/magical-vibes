package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlithBloodletter.class, AlphaMyr.class, GraniteShard.class})
class SlithBloodletterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        bloodletter.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when blocked without dealing damage to a player")
    void noCounterWhenBlocked() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        bloodletter.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not get a counter from noncombat damage")
    void noCounterOnNoncombatDamage() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        Permanent graniteShard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(graniteShard),
                null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regeneration ability creates a regeneration shield")
    void activatesRegeneration() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodletter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        bloodletter.setRegenerationShield(1);
        bloodletter.setBlocking(true);
        bloodletter.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AlphaMyr());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Slith Bloodletter");
        assertThat(findPermanent(player1, "Slith Bloodletter").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Slith Bloodletter").getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("A larger combat hit adds only one counter, after the trigger resolves")
    void largerCombatHitAddsOnlyOneCounter() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        bloodletter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bloodletter.setAttacking(true);

        resolveCombat(player1);

        harness.assertLife(player2, 17);
        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate regeneration while tapped and summoning sick")
    void regeneratesWhileTappedAndSummoningSick() {
        Permanent bloodletter = harness.addToBattlefieldAndReturn(player1, new SlithBloodletter());
        bloodletter.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bloodletter.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(bloodletter.getRegenerationShield()).isEqualTo(1);
        assertThat(bloodletter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated regeneration saves only its source from lethal damage")
    void activatedRegenerationSavesOnlyItsSource() {
        Permanent bloodletter = addCreatureReady(player1, new SlithBloodletter());
        Permanent otherBloodletter = addCreatureReady(player1, new SlithBloodletter());
        harness.addToBattlefield(player2, new GraniteShard());
        harness.addToBattlefield(player2, new GraniteShard());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bloodletter.isTapped()).isFalse();
        assertThat(otherBloodletter.getRegenerationShield()).isZero();

        harness.activateAbility(player2, 0, null, bloodletter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slith Bloodletter");
        assertThat(bloodletter.isTapped()).isTrue();
        assertThat(bloodletter.getMarkedDamage()).isZero();
        assertThat(bloodletter.getRegenerationShield()).isZero();
        assertThat(bloodletter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player2, 1, null, otherBloodletter.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bloodletter).doesNotContain(otherBloodletter);
        harness.assertInGraveyard(player1, "Slith Bloodletter");
    }
}
