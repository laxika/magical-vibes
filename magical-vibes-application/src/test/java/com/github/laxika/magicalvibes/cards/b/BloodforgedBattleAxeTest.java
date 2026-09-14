package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodforgedBattleAxe.class, GrizzlyBears.class, AirElemental.class})
class BloodforgedBattleAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage to a player creates a token copy")
    void combatDamageToPlayerCreatesTokenCopy() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bloodforged Battle-Axe"));
    }

    @Test
    @DisplayName("Blocked equipped creature does not create a token copy")
    void blockedCreatureDoesNotCreateTokenCopy() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = new Permanent(new AirElemental());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(countPermanents(player1, "Bloodforged Battle-Axe")).isOne();
    }

    private Permanent addAxeReady(Player player) {
        Permanent axe = new Permanent(new BloodforgedBattleAxe());
        axe.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(axe);
        return axe;
    }
}
