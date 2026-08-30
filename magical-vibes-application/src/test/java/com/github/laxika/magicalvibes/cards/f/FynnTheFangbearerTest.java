package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FynnTheFangbearerTest extends BaseCardTest {

    private Permanent addReady(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("A deathtouch creature dealing combat damage gives two poison counters")
    void deathtouchCreatureGivesTwoPoisonCounters() {
        addReady(new FynnTheFangbearer()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each deathtouch creature dealing combat damage triggers separately")
    void eachDeathtouchCreatureTriggersSeparately() {
        addReady(new FynnTheFangbearer()).setAttacking(true);
        addReady(new AmbushViper()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature without deathtouch does not trigger Fynn")
    void nonDeathtouchCreatureDoesNotTrigger() {
        addReady(new FynnTheFangbearer());
        addReady(new GrizzlyBears()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A blocked deathtouch creature does not trigger Fynn")
    void blockedDeathtouchCreatureDoesNotTrigger() {
        Permanent fynn = addReady(new FynnTheFangbearer());
        fynn.setAttacking(true);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
