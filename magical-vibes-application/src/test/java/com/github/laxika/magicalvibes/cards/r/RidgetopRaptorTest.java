package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RidgetopRaptor.class, FugitiveWizard.class})
class RidgetopRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent raptor = addCreatureReady(player1, new RidgetopRaptor());
        raptor.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Double strike destroys a small blocker before it can deal combat damage")
    void doubleStrikeKillsBlockerBeforeRegularCombatDamage() {
        Permanent raptor = addCreatureReady(player1, new RidgetopRaptor());
        addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ridgetop Raptor");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player2, 20);
    }
}
