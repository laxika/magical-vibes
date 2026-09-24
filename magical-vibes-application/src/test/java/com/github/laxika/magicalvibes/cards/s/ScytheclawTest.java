package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scytheclaw.class, GrizzlyBears.class})
class ScytheclawTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates and equips a Phyrexian Germ")
    void livingWeaponCreatesAndEquipsGerm() {
        castScytheclaw();

        Permanent scytheclaw = findPermanent(player1, "Scytheclaw");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(scytheclaw.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature's combat damage makes the player lose half their life, rounded up")
    void combatDamageMakesPlayerLoseHalfLifeRoundedUp() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = addScytheclawReady(player1);
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("No life loss trigger occurs when the equipped creature deals no combat damage to a player")
    void noTriggerWithoutCombatDamageToPlayer() {
        harness.setLife(player2, 22);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scytheclaw = addScytheclawReady(player1);
        scytheclaw.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    private void castScytheclaw() {
        harness.setHand(player1, List.of(new Scytheclaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addScytheclawReady(Player player) {
        Permanent permanent = new Permanent(new Scytheclaw());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
