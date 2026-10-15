package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Carbonize;
import com.github.laxika.magicalvibes.cards.s.ShorelineRanger;
import com.github.laxika.magicalvibes.cards.t.TorrentOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnElemental.class, Carbonize.class, ShorelineRanger.class, TorrentOfFire.class})
class DawnElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage that would be dealt to it")
    void preventsAllDamageToIt() {
        Permanent elemental = addCreatureReady(player1, new DawnElemental());
        harness.setHand(player2, List.of(new Carbonize()));

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Survives combat damage and still deals damage to a blocker")
    void survivesCombatAndDealsDamage() {
        Permanent elemental = addCreatureReady(player1, new DawnElemental());
        Permanent blocker = addCreatureReady(player2, new ShorelineRanger());

        elemental.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents damage greater than its toughness")
    void preventsDamageGreaterThanToughness() {
        Permanent elemental = addCreatureReady(player2, new DawnElemental());
        addCreatureReady(player1, new ShorelineRanger());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, elemental.getId());

        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elemental);
        harness.assertNotInGraveyard(player2, "Dawn Elemental");
    }

    @Test
    @DisplayName("Prevents damage while blocking and still damages the attacker")
    void preventsDamageWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new ShorelineRanger());
        Permanent elemental = addCreatureReady(player2, new DawnElemental());
        attacker.setAttacking(true);
        elemental.setBlocking(true);
        elemental.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elemental);
        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not prevent damage to other creatures")
    void doesNotProtectOtherCreatures() {
        Permanent elemental = addCreatureReady(player1, new DawnElemental());
        Permanent ranger = addCreatureReady(player1, new ShorelineRanger());
        harness.setHand(player2, List.of(new Carbonize()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, ranger.getId());

        assertThat(ranger.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental, ranger);
    }
}
