package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornetCobra.class, CrimsonKobolds.class})
class HornetCobraTest extends BaseCardTest {

    @Test
    void firstStrikeKillsAOneOneBeforeItDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new HornetCobra());
        attacker.setAttacking(true);

        CrimsonKobolds blockerCard = new CrimsonKobolds();
        blockerCard.setPower(1);
        blockerCard.setToughness(1);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Crimson Kobolds");
    }
}
