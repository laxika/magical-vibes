package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JungleTroll.class, HillGiant.class})
class JungleTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} grants a regeneration shield")
    void redActivationGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new JungleTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {G} grants a regeneration shield")
    void greenActivationGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new JungleTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Jungle Troll from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new JungleTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Jungle Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Jungle Troll dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new JungleTroll());
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Jungle Troll");
        harness.assertInGraveyard(player1, "Jungle Troll");
    }
}
