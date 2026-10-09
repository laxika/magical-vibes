package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.l.LightningStorm;
import com.github.laxika.magicalvibes.cards.p.PanglacialWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoverOfWinter.class, KjeldoranOutrider.class, LightningStorm.class, PanglacialWurm.class})
class CoverOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents age-counter damage from combat creatures to you and your creatures")
    void preventsCombatDamageToControllerAndCreatures() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 1);
        Permanent blocker = addReadyCreature(player1);
        Permanent blockedAttacker = addAttacker(player2);
        Permanent unblockedAttacker = addAttacker(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blockedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unblockedAttacker);
    }

    @Test
    @DisplayName("Prevents damage equal to all age counters")
    void scalesWithAgeCounters() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 2);
        addAttacker(player2);

        harness.setLife(player1, 20);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new LightningStorm()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The snow activation adds an age counter")
    void snowActivationAddsAgeCounter() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cumulative upkeep puts on an age counter and charges one snow mana per counter")
    void cumulativeUpkeepUsesSnowManaPerAgeCounter() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(1);

        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cover);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrifices itself when cumulative upkeep is not paid")
    void cumulativeUpkeepSacrificesIfNotPaid() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cover of Winter");
        harness.assertInGraveyard(player1, "Cover of Winter");
    }

    @Test
    @DisplayName("A trampling creature shares one prevention amount across its recipients")
    void sharesPreventionBetweenBlockerAndController() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 1);
        Permanent blocker = addReadyCreature(player1);
        Permanent attacker = addCreatureReady(player2, new PanglacialWurm());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);

        resolveCombat(player2);

        harness.handleCombatDamageAssigned(player2, 0,
                java.util.Map.of(blocker.getId(), 2, player1.getId(), 7));
        var preventionChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, preventionChoice.options().stream()
                .filter(option -> option.startsWith("Kjeldoran Outrider")).findFirst().orElseThrow());

        // The controller can divide prevention, but only one of the nine damage is prevented.
        int damageToPlayer = 20 - gd.getLife(player1.getId());
        assertThat(damageToPlayer + blocker.getMarkedDamage()).isEqualTo(8);
    }

    @Test
    @DisplayName("Zero age counters prevent no combat damage")
    void zeroAgeCountersPreventNothing() {
        harness.addToBattlefield(player1, new CoverOfWinter());
        addAttacker(player2);
        harness.setLife(player1, 20);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cumulative upkeep charges for existing age counters as well as the new counter")
    void cumulativeUpkeepIncludesExistingAgeCounters() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cover);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Combat prevention also protects your attacking creatures")
    void protectsAttackingCreaturesButNotOpposingBlockers() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 1);
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new KjeldoranOutrider());
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addReadyCreature(player);
        attacker.setAttacking(true);
        return attacker;
    }
}
