package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cinderbones.class, ZealousGuardian.class})
class CinderbonesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the ability on the stack")
    void activatingRegenPutsOnStack() {
        addCinderbonesReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCinderbonesReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent cinderbones = findPermanent(player1, "Cinderbones");
        assertThat(cinderbones.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wither deals combat damage to creatures as -1/-1 counters")
    void witherDealsMinusOneCountersToCreature() {
        Permanent cinderbones = addCinderbonesReady(player1);
        cinderbones.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, 2, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Regeneration shield saves Cinderbones from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCinderbonesReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Cinderbones");
        Permanent cinderbones = findPermanent(player1, "Cinderbones");
        assertThat(cinderbones.isTapped()).isTrue();
        assertThat(cinderbones.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cinderbones dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCinderbonesReady(player1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Cinderbones");
        harness.assertInGraveyard(player1, "Cinderbones");
    }

    private Permanent addCinderbonesReady(Player player) {
        return addCreatureReady(player, new Cinderbones());
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        ZealousGuardian card = new ZealousGuardian();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
