package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiliousSkulldwellerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gives the defending player a poison counter")
    void combatDamageGivesPoisonCounter() {
        harness.setLife(player2, 20);
        Permanent skulldweller = addReadySkulldweller();
        skulldweller.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hillGiant = new Permanent(new HillGiant());
        hillGiant.setSummoningSick(false);
        hillGiant.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(hillGiant);

        Permanent skulldweller = new Permanent(new BiliousSkulldweller());
        skulldweller.setSummoningSick(false);
        skulldweller.setBlocking(true);
        skulldweller.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(skulldweller);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Bilious Skulldweller");
    }

    private Permanent addReadySkulldweller() {
        Permanent perm = new Permanent(new BiliousSkulldweller());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
