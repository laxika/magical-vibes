package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Frostwielder;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealHaze.class, Frostwielder.class, GlacialRay.class})
class EtherealHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures")
    void preventsCombatDamageFromCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new Frostwielder());
        castEtherealHaze();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage from creatures to creatures")
    void preventsCombatDamageToCreatures() {
        Permanent attacker = addCreatureReady(player2, new Frostwielder());
        Permanent blocker = addCreatureReady(player1, new Frostwielder());
        castEtherealHaze();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents noncombat damage from creatures")
    void preventsNoncombatDamageFromCreatures() {
        harness.setLife(player1, 20);
        Permanent frostwielder = addCreatureReady(player2, new Frostwielder());
        castEtherealHaze();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(frostwielder), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventNoncreatureDamage() {
        harness.setLife(player1, 20);
        castEtherealHaze();

        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void castEtherealHaze() {
        harness.castFromHand(player1, new EtherealHaze(), "{W}");
        harness.passBothPriorities();
    }
}
