package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MossViper.class, NyxbornColossus.class})
class MossViperTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new MossViper());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());

        viper.setSummoningSick(false);
        viper.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(viper.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Deathtouch destroys a larger attacker when Moss Viper blocks")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent viper = harness.addToBattlefieldAndReturn(player2, new MossViper());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        viper.setBlocking(true);
        viper.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player2, "Moss Viper");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertNotOnBattlefield(player2, "Moss Viper");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deathtouch does not make damage to a player lethal")
    void unblockedViperDealsNormalPlayerDamage() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new MossViper());
        viper.setSummoningSick(false);
        viper.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Moss Viper");
    }
}
