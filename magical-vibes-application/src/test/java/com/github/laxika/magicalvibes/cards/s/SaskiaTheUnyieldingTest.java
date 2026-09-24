package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaskiaTheUnyielding.class, GrizzlyBears.class})
class SaskiaTheUnyieldingTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature's combat damage is dealt again to the chosen player")
    void repeatsEachCreatureCombatDamage() {
        Permanent saskia = addSaskia();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(saskia).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("The remembered player remains the destination even when it is not the defender")
    void usesRememberedPlayerInsteadOfDamagedPlayer() {
        Permanent saskia = addSaskia();
        saskia.setRememberedTargetPlayerId(player1.getId());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The triggered damage resolves after Saskia leaves the battlefield")
    void triggerUsesRememberedPlayerAfterSaskiaLeaves() {
        Permanent saskia = addSaskia();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(saskia);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addSaskia() {
        Permanent saskia = harness.enterBattlefieldAndReturn(player1, new SaskiaTheUnyielding());
        saskia.setSummoningSick(false);
        return saskia;
    }
}
