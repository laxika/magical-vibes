package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.k.KeeneyeAven;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RidgetopRaptor.class, FugitiveWizard.class, KeeneyeAven.class})
class RidgetopRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent raptor = addCreatureReady(player1, new RidgetopRaptor());
        raptor.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Double strike destroys a small blocker before it can deal combat damage")
    void doubleStrikeKillsBlockerBeforeRegularCombatDamage() {
        addCreatureReady(player1, new RidgetopRaptor());
        addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ridgetop Raptor");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocker surviving first-strike damage trades with the raptor in regular damage")
    void survivingBlockerDealsRegularDamageWhileRaptorDealsDamageAgain() {
        addCreatureReady(player1, new RidgetopRaptor());
        addCreatureReady(player2, new KeeneyeAven());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Ridgetop Raptor");
        harness.assertInGraveyard(player2, "Keeneye Aven");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Double strike also destroys an attacker before it can damage the blocking raptor")
    void doubleStrikeWorksWhileBlocking() {
        addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player2, new RidgetopRaptor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Ridgetop Raptor");
        harness.assertLife(player2, 20);
    }
}
