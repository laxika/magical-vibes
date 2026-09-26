package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BladeSliver;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ToxinSliver.class, BladeSliver.class, EnormousBaloth.class, FugitiveWizard.class})
class ToxinSliverTest extends BaseCardTest {

    @Test
    void destroyTriggerSurvivesToxinSliverDyingInCombat() {
        Permanent toxin = addCreatureReady(player1, new ToxinSliver());
        toxin.setAttacking(true);
        addCreatureReady(player2, new EnormousBaloth());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Toxin Sliver");
        harness.assertInGraveyard(player2, "Enormous Baloth");
    }

    @Test
    @DisplayName("Destroys a creature dealt combat damage by Toxin Sliver")
    void destroysCreatureDealtCombatDamage() {
        Permanent toxin = addCreatureReady(player1, new ToxinSliver());
        toxin.setAttacking(true);
        addCreatureReady(player2, new EnormousBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Enormous Baloth");
    }

    @Test
    @DisplayName("Grants the destroy trigger to other Slivers")
    void grantsDestroyTriggerToOtherSlivers() {
        addCreatureReady(player1, new ToxinSliver());
        Permanent sliver = addCreatureReady(player1, new BladeSliver());
        sliver.setAttacking(true);
        addCreatureReady(player2, new EnormousBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Enormous Baloth");
    }

    @Test
    @DisplayName("Cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent toxin = addCreatureReady(player1, new ToxinSliver());
        toxin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new EnormousBaloth());
        blocker.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Enormous Baloth");
    }

    @Test
    @DisplayName("Grants the destroy trigger to opposing Slivers")
    void grantsDestroyTriggerToOpposingSlivers() {
        addCreatureReady(player1, new ToxinSliver());
        addCreatureReady(player1, new EnormousBaloth());
        Permanent opposingSliver = addCreatureReady(player2, new BladeSliver());
        opposingSliver.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Enormous Baloth");
        harness.assertInGraveyard(player2, "Blade Sliver");
    }

    @Test
    @DisplayName("Does not grant the destroy trigger to non-Slivers")
    void doesNotGrantDestroyTriggerToNonSlivers() {
        Permanent nonSliver = addCreatureReady(player1, new FugitiveWizard());
        nonSliver.setAttacking(true);
        addCreatureReady(player1, new ToxinSliver());
        addCreatureReady(player2, new EnormousBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Enormous Baloth");
    }
}
