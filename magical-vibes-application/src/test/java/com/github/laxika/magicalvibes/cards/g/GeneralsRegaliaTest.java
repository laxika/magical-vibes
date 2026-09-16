package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralsRegalia.class, StingingBarrier.class, HengeGuardian.class})
class GeneralsRegaliaTest extends BaseCardTest {

    @Test
    void redirectsNextDamageFromChosenSourceToControlledCreature() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        barrier.untap();
        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void onlyRedirectsDamageFromTheChosenSource() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent otherSource = addCreatureReady(player1, new StingingBarrier());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherSource.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotRedirectDamageToAControlledCreature() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent damagedCreature = addCreatureReady(player1, new HengeGuardian());
        Permanent redirectCreature = addCreatureReady(player1, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, indexOf(player1, regalia), null, redirectCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, barrier.getId());

        harness.activateAbility(player1, indexOf(player1, barrier), null, damagedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    void redirectsCombatDamageFromChosenSource() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent redirectCreature = addCreatureReady(player1, new HengeGuardian());
        Permanent attacker = addCreatureReady(player2, new HengeGuardian());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, regalia), null, redirectCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void onlyTargetsCreatureYouControl() {
        Permanent regalia = addCreatureReady(player1, new GeneralsRegalia());
        Permanent opponentCreature = addCreatureReady(player2, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, regalia), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
