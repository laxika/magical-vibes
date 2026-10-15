package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HandOfSilumgar.class, ColossodonYearling.class})
class HandOfSilumgarTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hand = addCreatureReady(player1, new HandOfSilumgar());
        Permanent blocker = addCreatureReady(player2, new ColossodonYearling());
        hand.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Deathtouch destroys a larger attacking creature when blocking")
    void deathtouchWorksWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new ColossodonYearling());
        Permanent hand = addCreatureReady(player2, new HandOfSilumgar());
        attacker.setAttacking(true);
        hand.setBlocking(true);
        hand.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hand.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Zero power deals no damage and does not destroy a blocker with deathtouch")
    void zeroDamageDoesNotDestroyBlocker() {
        Permanent hand = addCreatureReady(player1, new HandOfSilumgar());
        Permanent blocker = addCreatureReady(player2, new ColossodonYearling());
        hand.setPowerModifier(-2);
        hand.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unblocked Hand of Silumgar deals ordinary damage to a player")
    void deathtouchDoesNotChangeDamageToPlayer() {
        Permanent hand = addCreatureReady(player1, new HandOfSilumgar());
        hand.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hand);
    }
}
