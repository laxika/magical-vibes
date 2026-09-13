package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Oraxid.class, LightningBolt.class, Unsummon.class, FireElemental.class, CrawWurm.class})
class OraxidTest extends BaseCardTest {

    @Test
    @DisplayName("Oraxid cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent oraxid = addCreatureReady(player2, new Oraxid());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oraxid.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Oraxid can be targeted by a non-red spell")
    void canBeTargetedByNonRedSpell() {
        Permanent oraxid = addCreatureReady(player2, new Oraxid());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, oraxid.getId());

        harness.assertInHand(player2, "Oraxid");
    }

    @Test
    @DisplayName("A red creature cannot block Oraxid")
    void redCreatureCannotBlock() {
        Permanent oraxid = addCreatureReady(player1, new Oraxid());
        oraxid.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FireElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(oraxid)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Oraxid is not dealt combat damage by a red creature")
    void redCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new FireElemental());
        attacker.setAttacking(true);

        Permanent oraxid = addCreatureReady(player2, new Oraxid());
        oraxid.setBlocking(true);
        oraxid.addBlockingTarget(0);

        resolveCombat();

        assertThat(oraxid.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oraxid");
    }

    @Test
    @DisplayName("Oraxid is dealt combat damage by a non-red creature")
    void nonRedCombatDamageIsNotPrevented() {
        Permanent attacker = addCreatureReady(player1, new CrawWurm());
        attacker.setAttacking(true);

        Permanent oraxid = addCreatureReady(player2, new Oraxid());
        oraxid.setBlocking(true);
        oraxid.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Oraxid");
        harness.assertInGraveyard(player2, "Oraxid");
    }
}
