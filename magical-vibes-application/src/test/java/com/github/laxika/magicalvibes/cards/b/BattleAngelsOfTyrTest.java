package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleAngelsOfTyr.class, Forest.class})
class BattleAngelsOfTyrTest extends BaseCardTest {

    @Test
    void combatDamageAppliesEachRiderWhenDamagedPlayerLeads() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);

        Permanent angels = harness.addToBattlefieldAndReturn(player1, new BattleAngelsOfTyr());
        angels.setSummoningSick(false);
        angels.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }

    @Test
    void combatDamageSkipsRidersWhenDamagedPlayerDoesNotLead() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent angels = harness.addToBattlefieldAndReturn(player1, new BattleAngelsOfTyr());
        angels.setSummoningSick(false);
        angels.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
